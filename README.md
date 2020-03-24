# CacheKit

## 简介

CacheKit 是一个Android端双缓存组件，可以方便的将数据缓存到内存中(通过androidx.collection.LruCache)或者是磁盘中(通过com.jakewharton.disklrucache.DiskLruCache)，同时可以为被缓存数据指定过期时间。

## 功能特点

- 支持内存缓存、磁盘缓存
- 支持带时效的数据缓存(指定数据过期时间)
- 支持类型转换器接口扩展用于支持更多数据类型被磁盘缓存

## 简单使用

1. 在project级别的build.gradle加入私有仓库地址：
	
	```groovy
	allprojects {
	    repositories {
	        ....
	        maven { url "https://dl.bintray.com/anymore0503/maven/" }
	    }
	}
	```
	
2. 在module级别的build.gradle加入如下依赖：

   ```groovy
   implementation 'com.anymore:cachekit:1.0.0'
   implementation 'com.jakewharton:disklrucache:2.0.2'
   ```

3. 通过DataCache.Builder构造DataCache实例：

   ```kotlin
   private val mDataCache by lazy {
           val dir = "$cacheDir${File.separator}cachekit"
           DataCache.Builder()
               .setDiskCacheDir(dir)
               .build()
   }
   ```

4. 存数据：

   ```kotlin
   //磁盘存int数据
   mDataCache.putToDisk("int",98856)
   //磁盘存double数据
   val d = 16.86694
   mDataCache.putToDisk("double",d)
   //磁盘存bitmap数据，缓存时间15秒
   val bitmap = BitmapFactory.decodeStream(resources.assets.open("crayon.jpg"))
   mDataCache.putToDisk("bitmap",bitmap,System.currentTimeMillis()+15*1000)
   //双缓存存JSONArray数据，缓存时间10秒
   val jsonArray = JSONArray()
   jsonArray.put(0,"abc")
   jsonArray.put(1,"www")
   jsonArray.put(2,"xyz")               
   mDataCache.put("jsonArray",jsonArray,System.currentTimeMillis()+10*1000)
   ```

5. 对应的取数据：

   ```kotlin
   //磁盘取int
   val value = mDataCache.getFromDisk<Int>("int")
   //磁盘取double
   val doubleValue = mDataCache.getFromDisk<Double>("double")
   //磁盘取bitmap
   val bitmap = mDataCache.getFromDisk<Bitmap>("bitmap")
   //双缓存取JSONArray
   val jsonArray = mDataCache.get<JSONArray>("jsonArray")
   ```

## 设计思路

磁盘缓存使用的是jakewharton的DiskLruCache来实现数据缓存的，但是DiskLruCache只能存储基本数据类型及其数组和Serializable接口的实现类型，对于应用程序里面很多自定义类型，如果非以上情况，会在写入磁盘的时候抛出异常：NotSerializableException。

那么可以如何解决？

- 在参考[Blankj](https://github.com/Blankj)的[AndroidUtilCode](https://github.com/Blankj/AndroidUtilCode)中[CacheDiskUtils.java](https://github.com/Blankj/AndroidUtilCode/blob/master/lib/utilcode/src/main/java/com/blankj/utilcode/util/CacheDiskUtils.java)的时候，在存储Bitmap，Parcelable对象的时候，他非常巧妙的将Bitmap和Parcelable 对象转换成为字节数组，然后存入文件中，在取数据的时候再将字节数组转换为原类型。所以我也考虑将不能Serializable的对象转换为字节数组存入磁盘，在取的时候再转回来。

- 在参考Retrofit的类型转换的思想的时候我得到启发：定义转换器接口（Converter）。如同Retrofit将网络请求和类型转换解耦一样，我们尝试通过接口将一个不支持序列化的数据类型转换为字节数组，而字节数组是支持序列化的，即可以将该数据存到磁盘中。这样即数据存储和类型转换解耦，只要增加相应类型转换器的支持，即可进行磁盘缓存此类型。

组件库设计：

1. 设计转换器接口[Converter.kt](https://github.com/anymao/CacheKit/blob/master/cachekit/src/main/java/com/anymore/cachekit/converters/Converter.kt)，可将不可序列化的数据类型降级为字节数组，也可将字节数组升级为指定数据类型。
2. 降级数据存储类[DiskCacheEntry.kt](https://github.com/anymao/CacheKit/blob/master/cachekit/src/main/java/com/anymore/cachekit/DiskCacheEntry.kt)，将第一步降级下来的字节数组以及其原类型(Class)固定下来，实际磁盘缓存的时候即为字节数组和原类型。
3. 类型转换适配器[TypeConverterAdapter.kt](https://github.com/anymao/CacheKit/blob/master/cachekit/src/main/java/com/anymore/cachekit/converters/TypeConverterAdapter.kt)，管理所有的Converter，帮助运行时寻找适配的转换器帮助数据升级和降级。
4. 时效缓存存储类[LifeLimitedCacheEntry.kt](https://github.com/anymao/CacheKit/blob/master/cachekit/src/main/java/com/anymore/cachekit/LifeLimitedCacheEntry.kt)，是对真正缓存类的一层包装，为了附加一个时间令牌，取数据的时候会检查时间令牌，如果过期，则不返还数据并且将原缓存数据清除。

在进行磁盘缓存的时候流程图如下:

![磁盘存储时候流程](http://cdn.1or1.icu/image/1585047256.png)

从磁盘取缓存数据时候流程如下:

![磁盘读取时候流程](http://cdn.1or1.icu/image/1585047064.png)



组件内部已经实现了几个常用的转换器可将Bitmap，JSONObject，JSONArray进行升降级转换，在实际使用中，如果想要存储一个数据类A，则有两种方式可选：

1. 将A实现序列化接口Serializable
2. 实现A向字节数组的转换AConverter，并且在DataCache.Builder构造时候注册进去。