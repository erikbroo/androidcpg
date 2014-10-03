<?php

if (!defined('IN_COPPERMINE')) die('Not in Coppermine...');
if (!defined('PLUGINMGR_PHP')) {
    define('PLUGINMGR_PHP', true);
}
if (!defined('ADMIN_PHP')) {
    define('ADMIN_PHP', true);
}
if (!defined('CORE_PLUGIN')) {
    define('CORE_PLUGIN', true);
}

// Add plugin_install action
$thisplugin->add_action('plugin_install','androidcpg_install');

// Add plugin_uninstall action
$thisplugin->add_action('plugin_uninstall','androidcpg_uninstall');

// Add plugin_cleanup action
$thisplugin->add_action('plugin_cleanup','androidcpg_cleanup');



// Install
function androidcpg_install() {
    return true;
}

function androidcpg_uninstall() {
    return true;
}

function androidcpg_cleanup($action) {

}
?>