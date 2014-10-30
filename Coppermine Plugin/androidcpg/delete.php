<?php
/*************************
  Coppermine Photo Gallery
  ************************
  Copyright (c) 2003-2014 Coppermine Dev Team
  v1.0 originally written by Gregory Demar

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License version 3
  as published by the Free Software Foundation.

  ********************************************
  Coppermine version: 1.5.30.1
**********************************************/

define('IN_COPPERMINE', true);
define('DELETE_PHP', true);
define('ALBMGR_PHP', true);
define('PROFILE_PHP', true);
chdir('../../');
require('include/init.inc.php');
require_once('androidcpg_enabled.php');

/**
 * Local functions definition
 */

$header_printed = false;
$need_caption = false;
$icon_array['ok'] = cpg_fetch_icon('ok', 1);

function output_table_header()
{
    global $header_printed, $need_caption, $lang_delete_php;

    $header_printed = true;
    $need_caption = true;
}


/**************************************************************************
* Picture manager functions
**************************************************************************/

function parse_pic_orig_sort_order($value)
{
    if (!preg_match("/(\d+)@(\d+)/", $value, $matches)) {
        return false;
    }

    return array(
        'pid' => (int) $matches[1],
        'pos' => (int) $matches[2],
    );
}

function parse_pic_list($value)
{
    return preg_split("/,/", $value, -1, PREG_SPLIT_NO_EMPTY);
}

function jsCheckFormToken(){
    global $lang_common, $lang_errors;
    //Check if the form token is valid
    if(!checkFormToken()){
        $dataArray = array(
            'message' => 'false',
            'title'   => $lang_common['error'],
            'description' => $lang_errors['invalid_form_token']
        );

        header("Content-Type: text/plain");
        echo json_encode($dataArray);
        exit;
    }
}

/**
 * Main code starts here
 */

if ($superCage->get->keyExists('what')) {
    $what = $superCage->get->getAlpha('what');
} elseif ($superCage->post->keyExists('what')) {
    $what = $superCage->post->getAlpha('what');
} else {
    cpg_die(CRITICAL_ERROR, $lang_errors['param_missing'], __FILE__, __LINE__);
}

switch ($what) {

case 'albmgr':

    if (!(GALLERY_ADMIN_MODE || USER_ADMIN_MODE)) {
        cpg_die(ERROR, $lang_errors['access_denied'], __FILE__, __LINE__);
    }

    if (!GALLERY_ADMIN_MODE) {

        //restrict to allowed categories of user
        //first get allowed categories

        $group_id = $USER_DATA['group_id'];
        $result = cpg_db_query("SELECT DISTINCT cid FROM {$CONFIG['TABLE_CATMAP']} WHERE group_id = $group_id");
        $rowset = cpg_db_fetch_rowset($result);
        mysql_free_result($result);

        //add allowed categories to the restriction
        if (USER_CAN_CREATE_PRIVATE_ALBUMS) {
            $restrict = "AND (category = '" . (FIRST_USER_CAT + USER_ID) . "'";
        } else {
            $restrict = "AND (0";
        }

        foreach ($rowset as $key => $value) {
            $restrict .= " OR category = '" . $value['cid'] . "'";
        }

        $restrict .= ")";

    } else {
        $restrict = '';
    }

    $returnOutput = ''; // the var that will later be shown as a result of the action performed
    $returnOutput .= '<table border="0" cellspacing="0" cellpadding="0" width="100%">';

    $sort_list_matched = $superCage->post->getMatched('sort_order', '/^[0-9@,]+$/');
    $orig_sort_order = parse_pic_list($sort_list_matched[0]);
    foreach ($orig_sort_order as $album) {
        $alb = parse_pic_orig_sort_order($album);
        $sort_array[$i] = $alb['aid'];
        if (count($alb) == 2) {
            $query = "UPDATE {$CONFIG['TABLE_ALBUMS']} SET pos = '{$alb['pos']}' WHERE aid = '{$alb['pid']}' $restrict LIMIT 1";
            cpg_db_query($query);
        } else {
            cpg_die(CRITICAL_ERROR, sprintf($lang_delete_php['err_invalid_data'], $sort_list_matched[0]), __FILE__, __LINE__);
        }
    }

    //prevent sorting of the albums if not admin or in own album
    $sorted_list = $superCage->post->getMatched('sort_order', '/^[0-9@,]+$/');

    //getting the category to redirect to album manager
    //$category = $superCage->get->getInt('cat');
    if ($superCage->get->keyExists('cat')) {
        $category = $superCage->get->getInt('cat');
    } elseif ($superCage->post->keyExists('cat')) {
        $category = $superCage->post->getInt('cat');
    }
    //get the action
    //$op = $superCage->get->getAlpha('op');
    if ($superCage->get->keyExists('op')) {
        $op = $superCage->get->getAlpha('op');
    } elseif ($superCage->post->keyExists('op')) {
        $op = $superCage->post->getAlpha('op');
    }
    //get the position
    //$position = $superCage->get->getInt('position');
    if ($superCage->get->keyExists('position')) {
        $position = $superCage->get->getInt('position');
    } elseif ($superCage->post->keyExists('position')) {
        $position = $superCage->post->getInt('position');
    }

    //get the album name
    //$get_album_name = trim($superCage->get->getEscaped('name'));
    if ($superCage->get->keyExists('name')) {
        $get_album_name = $superCage->get->getEscaped('name');
    } elseif ($superCage->post->keyExists('name')) {
        $get_album_name = $superCage->post->getEscaped('name');
    }
    //add the new album name to database
    if ($op == 'add') {
        if ($superCage->get->keyExists('uploader')) {
            $get_uploader = $superCage->get->_getValue('uploader');
        } elseif ($superCage->post->keyExists('uploader')) {
            $get_uploader = $superCage->post->_getValue('uploader');
        }
        if (!isset($get_uploader) || $get_uploader !== 'android'){
            jsCheckFormToken();
        }

        $user_id = USER_ID;

        if (!empty($get_album_name)) {
            //add the album to database
            $query = "INSERT INTO {$CONFIG['TABLE_ALBUMS']} (category, title, uploads, pos, description, owner) VALUES ('$category', '$get_album_name', 'NO', '{$position}', '', '$user_id')";
            cpg_db_query($query);

            //get the aid of added the albums
            $getAid = mysql_insert_id($CONFIG['LINK_ID']);

            $dataArray = array(
                'message' => 'true',
                'newAid'  => $getAid,
                'album_name' => $get_album_name,
            );
        } else {
            $dataArray = array(
                'message' => 'false',
                'title'  => $lang_errors['error'],
                'description' => $lang_albmgr_php['alb_need_name']
            );
        }

        header("Content-Type: text/plain");
        echo json_encode($dataArray);
    }

    break;

// Unknown command
default:
    cpg_die(CRITICAL_ERROR, $lang_errors['param_missing'], __FILE__, __LINE__);
}

?>