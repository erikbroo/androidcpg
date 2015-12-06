# Introduction #

AndroidCPG - Android File Upload to Coppermine Plugin


# Details #

Upload photos and videos to your coppermine gallery through the Android standard Share context menu

# Code repository moved to Github #

Google Code is shutting down, so we are slowly moving to GitHub. AndroidCPG sourcecode at Google Code is not maintained anymore.

Latest changes for AndroidCPG source code can be found at https://github.com/IsaNexusDev/androidcpg

# Instructions #

AndroidCPG Plugin - Allows Android CPG application to connect with the gallery's server. Allowed operations: create albums and upload files (Images and videos)

AndroidCPG.apk - Android application: Create albums and upload files (Images and videos) through the standard Android Share context menu.

The instructions are:

  1. Install the AndroidCPG Plugin to your Coppermine gallery or ask your Gallery's administrator to do it. If you don't have yet any Coppermine Gallery, look at Coppermine Gallery Website... It's amazing! :-)
  1. Install AndroidCPG to your Android device. Enable Unknown sources, download and install. AndroidCPG is not at GooglePlay™, But it is included at F-Droid (https://f-droid.org) Open Source App Repository. You can install AndroidCPG by installing https://f-droid.org/FDroid.apk to your device and searching for AndroidCPG, Or directly from https://f-droid.org/repo/com.isanexusdev.androidcpg_6.apk. If you need additional instructions, please read http://www.maketecheasier.com/install-applications-without-the-market/. For your safety and freedom, I also include the APK source code. You can choose to compile it yourself.
  1. Open the AndroidCPG main activity, configure it (Coppermine Gallery's host address, username and password) and exit.
  1. Go as example to the Android Gallery, navigate your photos/videos and select one (or several), press Share and choose AndroidCPG. At this point the AndroidCPG share activity will appear.
  1. Select the album to upload (or create a new one) and press Upload Photo/Video. Activity will automatically exit when uploads are done.


> Steps 1, 2 and 3 need to be done just once... Unless you want to login with a different username or to a different Coppermine Gallery.

> Every time you want to upload more videos or photos just repeat steps 4 and 5


OPTIONAL

  * Youtube, Vimeo and Vine videos can also be shared to your coppermine gallery by pressing Android Youtube, Vimeo or Vine application share button and choosing AndroidCPG in the context menu.

> For this to work be sure to install the Remote videos extension, see: http://forum.coppermine-gallery.net/index.php/topic,60195.0.html, http://forum.coppermine-gallery.net/index.php/topic,77788#msg376147 and http://forum.coppermine-gallery.net/index.php/topic,77788#msg377063 for more info and download the extension if you are interested in this feature.

  * At Video upload, also its preview image (thumbnail) will be uploaded.
> > In the case of local video upload, the thumbnail will be the frame at the half of the video... Meaning: in a 100 seconds video, the thumbnail will be the frame at second 50.
> > In the case of Youtube/Vimeo/Vine video upload, the thumbnail will be the video's original thumbnail at Youtube/Vimeo/Vine.


> For being able to upload these thumbnails the Custom Thumbnail plugin (http://forum.coppermine-gallery.net/index.php/topic,60272.0.html) needs to be installed and enabled in the target Coppermine Gallery.

> AndroidCPG will detect if Custom Thumbnail plugin is installed, if not, thumbnails extraction and upload will be just ignored.


Source code For AndroidCPG (both server and client side) is open and free for all. Modify as you want or need...

If you want to report bugs or just give ideas, feel free to write a comment at http://forum.coppermine-gallery.net/index.php/topic,77788, it will come to my E-Mail (Developer console).

# AndroidCPG Website #

http://androidcpg.mine.bz/


**Enjoy!**