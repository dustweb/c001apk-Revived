#!/usr/bin/env python3
"""Patch only exact known upstream patterns; reject unreviewed source changes."""
import os, pathlib, re, sys, hashlib, shutil
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
base = root/'app/src/main/java/com/example/c001apk/compose'
expected = {
    'logic/model/ClientRelease.kt':'121248d2055c012f9c39ac3e200f3e338b880acd87063f854e695cc936f8f21a',
    'logic/repository/ClientUpdateRepository.kt':'18c15795682b58dd2c1393cbb9fd126b572a4b6a3b2ad51c73717fdd4a574eab',
    'ui/update/ClientUpdateUi.kt':'98f01d7e00209c580caf9ef71fb91ff537e8e18f2f50349d75bed419a43839f0',
    'constant/Constants.kt':'46ed13d7edf5e1513f3991fbda3bb97b6a38a69f50d1fa4614c4f42ed72f329c',
}
for rel,digest in expected.items():
    path=base/rel
    if not path.is_file() or path.is_symlink() or hashlib.sha256(path.read_bytes()).hexdigest()!=digest:
        raise ValueError('upstream updater changed; manual review required: '+rel)
assets=pathlib.Path(__file__).resolve().parent/'updater'
shutil.copyfile(assets/'ClientRelease.kt',base/'logic/model/ClientRelease.kt')
const=base/'constant/Constants.kt'
s=const.read_text().replace('https://api.github.com/repos/frisk1127/c001apk-Revived/releases/latest','https://api.github.com/repos/dustweb/c001apk-Revived/releases?per_page=100')
const.write_text(s)
repo=base/'logic/repository/ClientUpdateRepository.kt'
s=repo.read_text().replace('暂未找到正式发布版本，可直接打开发布页查看。','暂未找到个人版发布，可直接打开发布页查看。')
repo.write_text(s)
ui=base/'ui/update/ClientUpdateUi.kt'
s=ui.read_text().replace('import com.example.c001apk.compose.constant.Constants.URL_RELEASES','import com.example.c001apk.compose.logic.model.ClientRelease.Companion.PERSONAL_RELEASES_URL as URL_RELEASES')
s=s.replace('通过 GitHub Releases 获取正式版','通过 GitHub Releases 检查个人版（含预发布版）').replace('启动时检查 GitHub 正式版','启动时检查个人版（含预发布版）').replace('GitHub 正式版：','个人版 Release：')
ui.write_text(s)
test=root/'app/src/test/java/com/example/c001apk/compose/logic/model/PersonalClientReleaseTest.kt'
if test.exists(): raise ValueError('test path already exists')
test.parent.mkdir(parents=True,exist_ok=True)
shutil.copyfile(assets/'PersonalClientReleaseTest.kt',test)
print('Patched upstream source for com.example.c001apk.composf; versionCode',code)
if os.environ.get('GITHUB_OUTPUT'):
    with open(os.environ['GITHUB_OUTPUT'],'a') as f: f.write('version_code='+str(code)+'\n')
