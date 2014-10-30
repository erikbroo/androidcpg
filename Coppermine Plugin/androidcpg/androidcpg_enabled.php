<?php
/*This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License version 3
  as published by the Free Software Foundation.

  ********************************************
  AndroidCPG version: 1.5.30.1
**********************************************/
if (!defined('IN_COPPERMINE')) die('Not in Coppermine...');
if ($CONFIG['enable_plugins'] == 1) {
  $not_enabled = true;
  foreach ($CPG_PLUGINS as $thisplugin) {
    if ($thisplugin->name == 'AndroidCPG'){
      $not_enabled = false;
      break;
    }
  }
  if ($not_enabled){
    cpg_die(ERROR, 'plugin not enabled', __FILE__, __LINE__);
  }
} else {
    cpg_die(ERROR, 'plugin not enabled', __FILE__, __LINE__);
}
?>
