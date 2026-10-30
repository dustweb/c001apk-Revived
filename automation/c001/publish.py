#!/usr/bin/env python3
"""Publish only verified signed assets; refuse mismatches and version rollback."""
import hashlib,json,os,pathlib,re,subprocess,tempfile,urllib.parse
FORK='dustweb/c001apk-Revived'; UP='frisk1127/c001apk-Revived'
rid=int(os.environ['UPSTREAM_ID']); tag=os.environ['UPSTREAM_TAG']; commit=os.environ['UPSTREAM_COMMIT']
pre=os.environ['UPSTREAM_PRERELEASE']=='true'; code=int(os.environ['VERSION_CODE'])
name='composf-upstream-'+str(rid)
if not re.fullmatch('[0-9a-f]{40}',commit): raise ValueError('invalid commit')

def gh(*args):
    return subprocess.run(['gh',*args],check=True,text=True,stdout=subprocess.PIPE).stdout

def api(path): return json.loads(gh('api',path))
up=api('/repos/'+UP+'/releases/'+str(rid))
if up.get('draft') or up.get('tag_name')!=tag or bool(up.get('prerelease'))!=pre:
    raise ValueError('upstream release changed or no longer published')
cur=api('/repos/'+UP+'/commits/'+urllib.parse.quote(tag,safe='')).get('sha')
if cur!=commit: raise ValueError('upstream tag moved since discovery')
rels=json.loads(gh('api','--paginate','--slurp','/repos/'+FORK+'/releases?per_page=100'))
rels=[r for page in rels for r in page]
for r in rels:
    if not r.get('draft') and r.get('tag_name','').startswith('composf-upstream-'):
        m=re.search(r'^C001_VERSION_CODE: (\d+)$',r.get('body') or '',re.M)
        if not m: raise ValueError('existing release missing version marker')
        if int(m.group(1))>=code: raise ValueError('refusing versionCode rollback or reuse')
existing=[r for r in rels if r.get('tag_name')==name]
if len(existing)>1: raise ValueError('duplicate destination release')
if existing and not existing[0].get('draft'): raise ValueError('release already published; no overwrite')
assetdir=pathlib.Path(os.environ['RELEASE_ASSET_DIR'])
assets=[assetdir/'c001apk-composf.apk',assetdir/'source-composf.tar.gz',assetdir/'SHA256SUMS.txt']
for p in assets:
    if not p.is_file() or p.stat().st_size==0: raise ValueError('missing asset '+p.name)
body=('Personal build of ['+UP+'](https://github.com/'+UP+'/releases/tag/'+urllib.parse.quote(tag,safe='')+').\n\n'
      'Upstream tag: `'+tag+'`  \nUpstream commit: `'+commit+'`  \n'
      'Application ID: `com.example.c001apk.composf`  \nC001_VERSION_CODE: '+str(code)+'\n\n'
      'This is an independently signed derivative, not an upstream release. '
      'Complete corresponding source is attached as source-composf.tar.gz; see LICENSE inside.\n')
with tempfile.TemporaryDirectory() as temp:
    notes=pathlib.Path(temp)/'notes.md'; notes.write_text(body)
    args=['release','create',name,'--repo',FORK,'--target',os.environ['GITHUB_SHA'],'--draft','--title','c001apk composf · '+tag,'--notes-file',str(notes),'--latest=false']
    if pre: args.append('--prerelease')
    if not existing: gh(*args)
    else:
        if bool(existing[0].get('prerelease'))!=pre: raise ValueError('draft prerelease mismatch')
        gh('release','edit',name,'--repo',FORK,'--notes-file',str(notes),'--title','c001apk composf · '+tag,'--latest=false')
    gh('release','upload',name,*map(str,assets),'--repo',FORK,'--clobber')
    dest=pathlib.Path(temp)/'download'; dest.mkdir()
    gh('release','download',name,'--repo',FORK,'--dir',str(dest),'--pattern','c001apk-composf.apk','--pattern','source-composf.tar.gz','--pattern','SHA256SUMS.txt')
    for p in assets:
        q=dest/p.name
        if not q.is_file() or hashlib.sha256(q.read_bytes()).digest()!=hashlib.sha256(p.read_bytes()).digest():
            raise ValueError('asset round-trip mismatch: '+p.name)
    gh('release','edit',name,'--repo',FORK,'--draft=false')
    final=api('/repos/'+FORK+'/releases/tags/'+urllib.parse.quote(name,safe=''))
    if final.get('draft') or bool(final.get('prerelease'))!=pre: raise ValueError('publication state mismatch')
    print('Published:',final['html_url'])
