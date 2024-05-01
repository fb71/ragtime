/***
 * Basis settings of the Common template. Based on Bootstrap.
 */

<#include "Roboto.css">
<#include "RobotoCondensed.css">
<#include "Ubuntu.css">

:root {
    --common-page-color: ${config.colors.pageForeground};
    --common-page-color-rgb: ${config.colors.pageForeground@rgb};
    --common-page-bg: ${config.colors.pageBackground};
    --common-page-bg-rgb: ${config.colors.pageBackground@rgb};
    --common-link-color: ${config.colors.link};
    --common-link-color-rgb: ${config.colors.link@rgb};
    --common-accent-color: ${config.colors.accent};
    --common-accent-color-rgb: ${config.colors.accent@rgb};
    --common-header-bg: ${config.colors.headerBackground};
    --common-header-color: ${config.colors.headerForeground};
    --common-footer-bg: ${config.colors.footerBackground};
    --common-footer-color: ${config.colors.footerForeground};
    
    --bs-font-sans-serif: RobotoC, sans-serif;
    --bs-body-color: ${config.colors.pageForeground};
    --bs-body-color-rgb: ${config.colors.pageForeground@rgb};
    --bs-body-bg: ${config.colors.pageBackground};
    --bs-body-bg-rgb: ${config.colors.pageBackground@rgb};
    --bs-link-color: ${config.colors.link};
    --bs-link-color-rgb: ${config.colors.link@rgb};
    --bs-link-hover-color: ${config.colors.link};
    --bs-link-hover-color-rgb: ${config.colors.link@rgb};
}
 
.navbar-nav {
    --bs-nav-link-color: ${config.colors.link};
    --bs-nav-link-color-rgb: ${config.colors.link@rgb};
    --bs-nav-link-hover-color: ${config.colors.link};
    --bs-nav-link-hover-color-rgb: ${config.colors.link@rgb};
}


.Editable:hover {
    /*border: 1px solid #b72a44;*/
    box-sizing: border-box;
    cursor: pointer;
    box-shadow: 0 0 4px inset rgba(230,0,20,80%);
    border-radius: 4px;
    filter: brightness(1.3);
    transition: 
        box-shadow 300ms,
        filter 300ms; 
}

