#!/usr/bin/env python3
"""Find unpublished upstream releases, including prereleases; fail closed on errors."""
import json, os, re, sys, urllib.parse, urllib.request

API='https://api.github.com'
CUTOFF='2026-10-30T01:41:55Z'
UPSTREAM='frisk1127/c001apk-Revived'
FORK='dustweb/c001apk-Revived'

def api(path):
    headers={'Accept':'application/vnd.github+json','User-Agent':'c001-release-follow','X-GitHub-Api-Version':'2022-11-28'}
    token=os.environ.get('GH_TOKEN')
    if token: headers['Authorization']='Bearer '+token
    with urllib.request.urlopen(urllib.request.Request(API+path,headers=headers),timeout=45) as r:
        return json.load(r)

def releases(repo):
    out=[]
    for n in range(1,21):
        page=api('/repos/'+repo+'/releases?per_page=100&page='+str(n))
        if not isinstance(page,list): raise ValueError('invalid releases response')
        out.extend(page)
        if len(page)<100: return out
    raise RuntimeError('release list exceeds 2000; manual review required')

def discover():
    existing={r.get('tag_name') for r in releases(FORK) if not r.get('draft')}
    pending=[]
    hotfix=os.environ.get('C001_HOTFIX') == 'true'
    if hotfix and 'composf-upstream-429222606-fix-update-v1' in existing:
        raise ValueError('updater hotfix is already published')
    for r in releases(UPSTREAM):
        if r.get('draft') or not r.get('published_at') or r['published_at']<CUTOFF: continue
        rid=r.get('id'); tag=r.get('tag_name')
        if not isinstance(rid,int) or not isinstance(tag,str) or not tag or len(tag)>180: raise ValueError('bad release id or tag')
        dest='composf-upstream-'+str(rid)
        if hotfix and rid != 429222606: continue
        if not hotfix and dest in existing: continue
        commit=api('/repos/'+UPSTREAM+'/commits/'+urllib.parse.quote(tag,safe='')).get('sha','')
        if not re.fullmatch('[0-9a-f]{40}',commit): raise ValueError('bad upstream commit')
        pending.append({'id':rid,'tag':tag,'commit':commit,'prerelease':bool(r.get('prerelease')),'published':r['published_at'],'hotfix':hotfix})
    if hotfix and len(pending) != 1: raise ValueError('original upstream release not found')
    pending.sort(key=lambda r:(r['published'],r['id']))
    if len(pending)>100: raise RuntimeError('over 100 pending releases; manual review required')
    for index,r in enumerate(pending): r['index']=index
    return pending

if __name__=='__main__':
    pending=discover()
    if os.environ.get('GITHUB_OUTPUT'):
        with open(os.environ['GITHUB_OUTPUT'],'a') as f:
            f.write('matrix='+json.dumps({'include':pending},separators=(',',':'))+'\n')
            f.write('count='+str(len(pending))+'\n')
    print('Pending upstream releases:',len(pending))
    for r in pending: print(r['id'],r['tag'],r['commit'], 'prerelease='+str(r['prerelease']))
