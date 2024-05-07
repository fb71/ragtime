/***
 * Settings of the fb71 template.
 */
:root {
   --bs-box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.5);
}

a {
    text-decoration: none;
}
a:hover {
/*    filter: brightness(0.9) saturate(1.5);  */
    text-decoration: underline;
}

.IHeader {
    height: 180px;
    background-repeat: no-repeat;
    background-position: center center;
    background-size: cover;
    margin-bottom: 90px;
    box-shadow: 0 0px 6px rgb(0 0 0 / 40%);
}

.IHeader h1 {
    text-shadow: 0px 1px 5px rgb(0 0 0 / 70%);
    text-align: right;
    padding-top: 2rem;
    margin-right: calc(var(--bs-gutter-x) * .5);    
    color: var(--common-header-color);
}
    
.IHeader .IFace {
    position: absolute;
    top: 110px;
    width: 140px;
    height: 140px;
    border-radius: 100px;
    margin-left: calc(var(--bs-gutter-x) * .5);
    border: 3px solid var(--common-page-bg);
}

.IHeader .IFaceBordered {
    width: 100%;
    height: 100%;
    border-radius: 100px;
    //border: 2px solid #cdcdcd;
}

@media (max-width: 768px) {
  .IHeader {
    height: 120px;
    margin-bottom: 80px;
  }
  .IHeader h1 {
    padding-top: 1rem;
  }
  .IHeader .IFace {
    top: 70px;
    width: 110px;
    height: 110px;
  }
}

.lead h1, h2 {
    margin-bottom: 0px;
}