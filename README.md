## Android版本更新
[![](https://www.jitpack.io/v/NewHuLe/AppUpdate.svg)](https://www.jitpack.io/#NewHuLe/AppUpdate)
[![](https://github.com/NewHuLe/AppUpdate/blob/master/author/author_jianshu.svg)](https://www.jianshu.com/u/e87d858e89a4)
[![](https://github.com/NewHuLe/AppUpdate/blob/master/author/author_juejin.svg)](https://juejin.im/user/5823e16c5bbb50005907fdb2/posts) 

原生DownloadManager实现版本的检测更新，自由控制下载进度、下载失败弹框、是否强制更新、是否MD5校验、完美适配Android M/N/O/P/Q
## 功能介绍
- 兼容AndroidX，项目已经迁移到Androidx
- 适配Android M，处理关于存储文件的运行时权限
- 适配Android N，安卓增强了文件访问的安全性，利用FileProvider来访问文件
- 适配Android O，增加未知来源应用的安装提示
- 适配Android Q，关于Q增加沙箱，改变了应用程序访问设备外部存储上文件的方式如SD卡
- 支持静默下载，下载完毕自动弹出安装
- 支持下载进度监听与下载失败提示
- 支持强制更新，未更新无法使用应用
- 支持MD5文件防篡改及完整性校验
- 支持自定义更新提示界面
- 下载失败支持通过系统浏览器下载
## 效果图
![](https://github.com/NewHuLe/AppUpdate/blob/master/screenshots/%E5%BC%BA%E5%88%B6%E6%9B%B4%E6%96%B0.jpg)
![](https://github.com/NewHuLe/AppUpdate/blob/master/screenshots/%E9%9D%9E%E5%BC%BA%E5%88%B6%E6%9B%B4%E6%96%B0.jpg)
## 关于使用
- 工程build.gradle目录添加
```
	allprojects {
		repositories {
			...
			maven { url 'https://www.jitpack.io' }
		}
	}
```
- 项目build.gradle文件添加
```
 	dependencies {
	       implementation 'com.github.NewHuLe:AppUpdate:v1.4'
	}
```
- 代码调用示例，简单写法，更多配置参考demo
```
 UpdateManager updateManager = new UpdateManager(MainActivity.this);
        AppUpdate appUpdate = new AppUpdate.Builder()
                //更新地址（必须）
                .newVersionUrl("https://imtt.dd.qq.com/16891/8EC4E86B648D57FDF114AF5D3002C09B.apk")
                // 版本号（非必须）
                .newVersionCode("v1.4")
                // 文件大小（非必须）
                .fileSize("5.8M")
                // 更新内容（非必填，默认“1.用户体验优化\n2.部分问题修复”）
                .updateInfo("1.用户体验优化\n2.部分问题修复")
                .build();
        updateManager.startUpdate(appUpdate, this);
```
- 下载完成后，默认安装已经发出允许安装未知来源应用权限申请，如果拒绝权限，需要在更新页面增加权限拒绝后的回调处理，详情可见demo
```
 /**
     * 检测到无权限安装未知来源应用，回调接口中需要重新请求安装未知应用来源的权限
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void applyAndroidOInstall() {
        //请求安装未知应用来源的权限
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, INSTALL_PACKAGES_REQUESTCODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 8.0的权限请求结果回调
        if (requestCode == INSTALL_PACKAGES_REQUESTCODE) {
            // 授权成功
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                installApkAgain();
            } else {
                // 授权失败，引导用户去未知应用安装的界面
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    //注意这个是8.0新API
                    Uri packageUri = Uri.parse("package:" + getPackageName());
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageUri);
                    startActivityForResult(intent, GET_UNKNOWN_APP_SOURCES);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //8.0应用设置界面未知安装开源返回时候
        if (requestCode == GET_UNKNOWN_APP_SOURCES) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                boolean allowInstall = getPackageManager().canRequestPackageInstalls();
                if (allowInstall) {
                    installApkAgain();
                } else {
                    Toast.makeText(MainActivity.this, "您拒绝了安装未知来源应用，应用暂时无法更新！", Toast.LENGTH_SHORT).show();
                    if (0 != updateUtils.getAppUpdate().getForceUpdate()) {
                        forceExit();
                    }
                }
            }
        }
    }
```
- 更高级调用写法（详细的配置请查看AppUpdate参数）：
```
 UpdateManager updateManager = new UpdateManager(MainActivity.this);
        // 更新的数据参数
        AppUpdate appUpdate = new AppUpdate.Builder()
                //更新地址（必传）
                .newVersionUrl("https://imtt.dd.qq.com/16891/apk/5CACCB57E3F02E46404D27ABAA85474C.apk")
                // 版本号（非必填）
                .newVersionCode("v1.4")
                // 通过传入资源id来自定义更新对话框，注意取消更新的id要定义为btnUpdateLater，立即更新的id要定义为btnUpdateNow（非必填）
                .updateResourceId(R.layout.dialog_update)
                // 更新的标题，弹框的标题（非必填，默认为应用更新）
                .updateTitle(R.string.update_title)
                // 更新内容的提示语，内容的标题（非必填，默认为更新内容）
                .updateContentTitle(R.string.update_content_lb)
                // 更新内容（非必填，默认“1.用户体验优化\n2.部分问题修复”）
                .updateInfo("1.用户体验优化\n2.部分问题修复")
                // 文件大小（非必填）
                .fileSize("5.8M")
                //是否采取静默下载模式（非必填，只显示更新提示，后台下载完自动弹出安装界面），否则，显示下载进度，显示下载失败
                .isSilentMode(true)
                //是否强制更新（非必填，默认不采取强制更新，否则，不更新无法使用）
                .forceUpdate(0)
                .build();
        updateManager.startUpdate(appUpdate, this);
```
## 混淆配置
```
-keep class com.open.hule.library.entity.** { *; }
```
## 意见收集，下个版本即将优化
1.增加Kotlin语言版本
## 更新日志
- v1.4  
1.非静默下载模式下，按下Home键，此时系统会调用onSaveInstance(),对弹框造成的影响优化  
2.关于下载进度可能出现负值优化，由于大文件进度换算超出了int范围，改用long类型  
3.若本地已经下载最新apk文件，点击立即更新，覆盖安装时，关闭提醒框  
4.使用Java7新的try-with-resources ，凡是实现了AutoCloseable接口的可自动close()，所以无需在手动close()  
5.库内部其他健壮性优化  
- v1.3  
1.非静默下载模式下，将下载进度条与下载失败融合进更新提醒框，不在单独开启下载进度弹框与下载失败弹框  
2.自动检测本地是否有最新的安装文件，如果有直接安装，无需下载  
3.库内部优化（AppUpdateUtils更名为UpdateManager） 
- v1.2  
1.更改通知栏的默认下载显示（为VISIBILITY_VISIBLE_NOTIFY_COMPLETED），大部分机型下载中会在通知栏显示下载进度，除了oppo机型通知栏只显示有一个任务正在下载中...,没有显示下载进度，这个是由系统自身的下载所决定。
- v1.1  
1.优化静默下载  
2.版本号与文件大小改为非必传  
- v1.0  
1.初次提交  
## License
```
Copyright 2019 胡乐

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
