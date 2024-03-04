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
    --bs-font-sans-serif: Roboto, sans-serif;
    --bs-body-color: ${config.colors.pageForeground};
    --bs-body-color-rgb: ${config.colors.pageForeground@rgb};
    --bs-body-bg: ${config.colors.pageBackground};
    --bs-body-bg-rgb: ${config.colors.pageBackground@rgb};
    --bs-link-color: ${config.colors.link};
    --bs-link-color-rgb: ${config.colors.link@rgb};
}
header {
    background-color: ${config.colors.headerBackground};
    color:${config.colors.headerForeground};
}
.navbar-nav {
    --bs-nav-link-color: ${config.colors.link};
}            
.nav-link:hover {
    --bs-nav-link-color: ${config.colors.link};
    text-decoration: underline;
}            
