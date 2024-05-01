/***
 * Basis settings of the First template.
 */
 
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
/*    filter: brightness(0.9) saturate(1.5);  */
    text-decoration: underline;
}
.CContent a[href^="http"]::after,
.CContent a[href^="mailto"]::after,
.CContent a[href^="https://"]::after
{
  content: "";
  width: 11px;
  height: 11px;
  margin-left: 4px;
  margin-bottom: 1px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' fill='rgb(${config.colors.link@rgb})' viewBox='0 0 16 16'%3E%3Cpath fill-rule='evenodd' d='M8.636 3.5a.5.5 0 0 0-.5-.5H1.5A1.5 1.5 0 0 0 0 4.5v10A1.5 1.5 0 0 0 1.5 16h10a1.5 1.5 0 0 0 1.5-1.5V7.864a.5.5 0 0 0-1 0V14.5a.5.5 0 0 1-.5.5h-10a.5.5 0 0 1-.5-.5v-10a.5.5 0 0 1 .5-.5h6.636a.5.5 0 0 0 .5-.5z'/%3E%3Cpath fill-rule='evenodd' d='M16 .5a.5.5 0 0 0-.5-.5h-5a.5.5 0 0 0 0 1h3.793L6.146 9.146a.5.5 0 1 0 .708.708L15 1.707V5.5a.5.5 0 0 0 1 0v-5z'/%3E%3C/svg%3E");
  background-position: center;
  background-repeat: no-repeat;
  background-size: contain;
  display: inline-block;
}

.CHeader {
    background-color: var(--common-header-bg);
    color: var(--common-header-color);
    /* text-align: center; */
    /* font-weight: bolder; */
    padding-top: 2rem;
    padding-bottom: 2rem;
}

.CNavbar {
    margin-bottom: 1.0rem;
}

.CNavbar ul {
    margin-left: auto;
    /* margin-right: auto; */
}

.CAside {
    color: var(--common-accent-color);
}

.CAside.CStart {
    padding-left: 3rem;
    padding-right: 1rem;
    padding-top: 3rem;
    padding-bottom: 3rem;
    margin-left: 3rem;
}

.CAside.CEnd {
    padding-top: 1.5rem;
    margin-top: 1.5rem;
}

.CFooter {
    background-color: var(--common-footer-bg);
    color: var(--common-footer-color);
    padding-top: 0.5rem;
    padding-bottom: 0.5rem;
    text-align: center;
}

@media (min-width: 768px) {
  .CFooter {
    position: fixed;
    bottom: 0px;
    left: 0px;
    width: 100%;
  }
  .CContent {
    padding-bottom: 3rem;
  }
}

