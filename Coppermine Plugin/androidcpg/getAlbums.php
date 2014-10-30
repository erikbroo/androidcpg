<?php
/*This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License version 3
  as published by the Free Software Foundation.

  ********************************************
  AndroidCPG version: 1.5.30.1
**********************************************/

// Confirm we are in Coppermine and set the language blocks.
define('IN_COPPERMINE', true);
define('UPLOAD_PHP', true);
define('DB_INPUT_PHP', true);
define('ADMIN_PHP', true);
chdir('../../');
// Call basic functions, etc.
require('include/init.inc.php');
require_once('androidcpg_enabled.php');
require('include/picmgmt.inc.php');

// Check to see if user can upload pictures.  Quit with an error if user cannot.
if (!USER_CAN_UPLOAD_PICTURES && !USER_CAN_CREATE_ALBUMS) {
    die('error');
}

// Globalize $CONFIG
global $CONFIG, $USER, $lang_upload_php, $upload_form, $max_file_size;


//################################# MAIN CODE BLOCK ##################################################

// Check whether we are getting album id through _GET or _POST
if ($superCage->get->keyExists('album')) {
    $sel_album = $superCage->get->getInt('album');
} elseif ($superCage->post->keyExists('album')) {
    $sel_album = $superCage->post->getInt('album');
} else {
    $sel_album = 0;
}

// Get public and private albums, and set maximum individual file size.

if (GALLERY_ADMIN_MODE) {
    $public_albums = cpg_db_query("SELECT aid, title, cid, name FROM {$CONFIG['TABLE_ALBUMS']} INNER JOIN {$CONFIG['TABLE_CATEGORIES']} ON cid = category WHERE category < " . FIRST_USER_CAT." ORDER BY aid DESC");
    //select albums that don't belong to a category
    $public_albums_no_cat = cpg_db_query("SELECT aid, title FROM {$CONFIG['TABLE_ALBUMS']} WHERE category = 0 "." ORDER BY aid DESC");
} else {
    $public_albums = cpg_db_query("SELECT aid, title, cid, name FROM {$CONFIG['TABLE_ALBUMS']} INNER JOIN {$CONFIG['TABLE_CATEGORIES']} ON cid = category WHERE category < " . FIRST_USER_CAT . " AND ((uploads='YES' AND (visibility = '0' OR visibility IN ".USER_GROUP_SET." OR alb_password != '')) OR (owner=".USER_ID."))"." ORDER BY aid DESC");
    //select albums that don't belong to a category
    $public_albums_no_cat = cpg_db_query("SELECT aid, title FROM {$CONFIG['TABLE_ALBUMS']} WHERE category = 0 AND ((uploads='YES' AND (visibility = '0' OR visibility IN ".USER_GROUP_SET." OR alb_password != '')) OR (owner=".USER_ID."))"." ORDER BY aid DESC");
}


if (mysql_num_rows($public_albums)) {
    $public_albums_list = cpg_db_fetch_rowset($public_albums);
} else {
    $public_albums_list = array();
}

//do the same for non-categorized albums
if (mysql_num_rows($public_albums_no_cat)) {
    $public_albums_list_no_cat = cpg_db_fetch_rowset($public_albums_no_cat);
} else {
    $public_albums_list_no_cat = array();
}

//merge the 2 album arrays
$public_albums_list = array_merge($public_albums_list, $public_albums_list_no_cat);


if (USER_ID) {
    $user_albums = cpg_db_query("SELECT aid, title FROM {$CONFIG['TABLE_ALBUMS']} WHERE category='" . (FIRST_USER_CAT + USER_ID) . "' ORDER BY aid DESC");
    if (mysql_num_rows($user_albums)) {
        $user_albums_list = cpg_db_fetch_rowset($user_albums);
    } else {
        $user_albums_list = array();
    }

    $user_albums_last_pos = cpg_db_query("SELECT pos FROM {$CONFIG['TABLE_ALBUMS']} WHERE category='" . (FIRST_USER_CAT + USER_ID) . "' ORDER BY pos DESC LIMIT 1");
    if (mysql_num_rows($user_albums_last_pos)) {
        $last_pos = cpg_db_fetch_row($user_albums_last_pos)['pos'] + 1;
    } else {
        $last_pos = 100;
    }
} else {
    $user_albums_list = array();
}
echo("OK\r\n");
if (USER_CAN_CREATE_ALBUMS) {
    echo("cancreate\r\n");
} else {
    echo("cantcreate\r\n");
}

if (USER_ID) {
    echo((FIRST_USER_CAT + USER_ID)."\r\n");
    echo($last_pos."\r\n");
} else {
    echo("0\r\n");
    echo("100\r\n");
}

if (!count($public_albums_list) && !count($user_albums_list)) {
    echo "noalbums";
    exit;
}
foreach ($public_albums_list as $public_album){
    echo $public_album['aid']."|sep|".$public_album['title']."|sep|public\r\n";
}
foreach ($user_albums_list as $user_album){
    echo $user_album['aid']."|sep|".$user_album['title']."|sep|private\r\n";
}
//form_alb_list_box("", "");

?>
