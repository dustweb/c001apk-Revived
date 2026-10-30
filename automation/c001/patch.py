#!/usr/bin/env python3
"""Patch only exact known upstream patterns; reject unreviewed source changes."""
import os, pathlib, re, sys
root=pathlib.Path(sys.argv[1]).resolve()
run=int(os.environ['GITHUB_RUN_NUMBER']); index=int(os.environ['RELEASE_INDEX'])
code=1000000+run*1000+index
if not (1<=run and 0<=index<100 and code<=2100000000): raise ValueError('unsafe versionCode')
changes={
'app/build.gradle.kts':[
 ('applicationId = "com.example.c001apk.compose"','applicationId = "com.example.c001apk.composf"'),
 ('versionCode = gitCommitCount','versionCode = '+str(code)),
 ('signingConfig = config ?: signingConfigs["debug"]','signingConfig = null')],
'app/src/main/java/com/example/c001apk/compose/util/LauncherIconManager.kt':[
 ('ComponentName(context.packageName, "${context.packageName}.$alias")','ComponentName(context.packageName, "com.example.c001apk.compose.$alias")')],
'app/src/main/java/com/example/c001apk/compose/ui/home/app/AppListViewModel.kt':[
 ('info.packageName != "com.example.c001apk.compose"','info.packageName != c001Application.packageName')],
}
manifest=(root/'app/src/main/AndroidManifest.xml').read_text()
for needle in ('android:name=".RevivedLauncher"','android:name=".ClassicLauncher"','android:authorities="${applicationId}.fileprovider"'):
    if manifest.count(needle)!=1: raise ValueError('unexpected manifest: '+needle)
for rel,replacements in changes.items():
    path=root/rel
    if not path.is_file() or path.is_symlink(): raise ValueError('invalid source path '+rel)
    text=path.read_text()
    if rel=='app/build.gradle.kts':
        if text.count('namespace = "com.example.c001apk.compose"')!=1 or 'applicationIdSuffix' in text:
            raise ValueError('unexpected namespace or applicationIdSuffix')
    for old,new in replacements:
        if text.count(old)!=1: raise ValueError('upstream patch mismatch: '+rel+' '+old)
        text=text.replace(old,new,1)
    path.write_text(text)
print('Patched upstream source for com.example.c001apk.composf; versionCode',code)
if os.environ.get('GITHUB_OUTPUT'):
    with open(os.environ['GITHUB_OUTPUT'],'a') as f: f.write('version_code='+str(code)+'\n')
