<#--
  Basis settings of the Common template. Based on Bootstrap.
-->
@font-face {
  font-family: 'Roboto';
  font-weight: normal;
  font-style: normal;
  src: url('../fonts/Roboto-Regular.woff2') format('woff2');
}
@font-face {
  font-family: 'Roboto';
  font-weight: 500;
  font-style: normal;
  src: url('../fonts/Roboto-Medium.woff2');
}
@font-face {
  font-family: 'Roboto';
  font-weight: bold;
  font-style: normal;
  src: url('../fonts/Roboto-Bold.woff2');
}

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
    
    --bs-font-sans-serif: Roboto, sans-serif;
    --bs-body-color: ${config.colors.pageForeground};
    --bs-body-color-rgb: ${config.colors.pageForeground@rgb};
    --bs-body-bg: ${config.colors.pageBackground};
    --bs-body-bg-rgb: ${config.colors.pageBackground@rgb};
    --bs-link-color: ${config.colors.link};
    --bs-link-color-rgb: ${config.colors.link@rgb};
}
header {
    background-color: var(--common-header-bg);
    color: var(--common-header-color);
}
aside {
    color: var(--common-accent-color);
}
.navbar-nav {
    --bs-nav-link-color: var(--bs-link-color);
}            
.nav-link:hover {
    --bs-nav-link-color: var(--bs-link-color);
    text-decoration: underline;
}            
