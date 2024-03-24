<#--
  Basis settings of the Common template. Based on Bootstrap.
-->

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
    
    --bs-font-sans-serif: Roboto, sans-serif;
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

a {
    text-decoration: none;
}
a:hover {
<#--     filter: brightness(0.9) saturate(1.5);  -->
    text-decoration: underline;
}

.CHeader {
    background-color: var(--common-header-bg);
    color: var(--common-header-color);
}

.CFooter {
    background-color: var(--common-footer-bg);
    color: var(--common-footer-color);
}

aside {
    color: var(--common-accent-color);
}
