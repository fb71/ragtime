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

.CEditable {
    box-sizing: content-box;
    /* cursor: pointer; */
    transition: 
        box-shadow 300ms,
        filter 300ms,
        transform 300ms;
    
    /* disable text selection on long tap */
    user-select:none;
    -webkit-touch-callout:none;
    -webkit-user-select:none;
    -khtml-user-select:none;
    -moz-user-select:none;
    -ms-user-select:none;
    -webkit-tap-highlight-color:rgba(0,0,0,0);
}

.CEditable:hover {
    /* border: 1px dashed #b72a44; */
    /* box-shadow: 0 0 4px inset rgba(230,0,20,80%); */
    border-radius: 4px;
    filter: brightness(1.1);
    /*transform: scale(1.05);*/
}

.CBadge {
    position: absolute;
    top: 0px;
    right: 0px;
    width: 1rem;
    height: 1rem;
    
    border-radius: 1rem;
    background-color: #f78b1c;
    border: 2px solid #ffc79c;
    box-shadow: 0 3px 3px rgba(0,0,0,0.5);
}

/* CFooter ************************************************/

.CFooter {
    background-color: var(--common-footer-bg);
    color: var(--common-footer-color);
    padding-top: 0.5rem;
    padding-bottom: 0.5rem;
    text-align: center;
    margin-bottom: 0px !important;
}

@media (min-width: 768px) {
  .CFooter {
    position: fixed;
    bottom: 0px;
    left: 0px;
    width: 100%;
  }
}
