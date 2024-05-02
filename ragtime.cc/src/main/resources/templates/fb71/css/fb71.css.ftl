/***
 * Settings of the fb71 template.
 */
:root {
   --bs-box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.5);
}

.CHeader {
    padding-top: 1rem;
    padding-bottom: 1rem;
    padding-left: 2rem;
}

.CHeader h1 {
    margin: 0;
    line-height: 1;
}

.CNavbar {
    --bs-navbar-padding-y: 0rem;
    margin-bottom: 0rem;
}

.CNavbar ul {
    margin-right: auto;
}

.CNavbar .btn {
    --bs-btn-bg: #333338;
}

.CContent {
    --bs-gutter-x: 1.5rem;
    --bs-gutter-y: 1.5rem;
    margin-top: 3rem;
}

/** Sollten das lieber gutter sein?*/
.CSection {
    margin: 4rem 0;
}

.lead {
    font-size: calc(1.325rem + .9vw);
    font-weight: normal;
    line-height: 1.2;
    text-align: center;
}

.card {
    --bs-card-spacer-y: 0.5rem;
    /*--bs-card-spacer-x: 0.5rem;*/

    /*filter: brightness(1.2);*/
    background-color: #333338;
    font-size: smaller;
}

.card img {
    padding: 0.8rem;
}

@media (min-width: 768px) {
  .card {
    --bs-card-spacer-y: 1rem;
    --bs-card-spacer-x: 0.5rem;
  }
}
