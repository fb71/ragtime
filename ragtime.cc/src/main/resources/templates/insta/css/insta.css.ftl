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
    box-shadow: 0 0px 6px rgb(0 0 0 / 40%);
    
    max-width: 1280;
    margin-left: auto;
    margin-right: auto;
}

.IBgImage {
    background-repeat: no-repeat;
    background-position: center center;
    background-size: cover;
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

.ITopics {
    padding-left: 220px;
}

.ITopicsScroller {
    overflow-x: scroll;
    scrollbar-width: none;
    /*box-shadow: 0px 0 15px inset rgba(${config.colors.pageBackground@rgb},100%);*/
}
.ITopicsScroller::-webkit-scrollbar {
  display: none;
}

.ITopic {
    margin-top: 8px;
    --bs-gutter-x: 2.5rem;
    font-size: smaller;
    text-align: center;
    color: var(--common-link-color);
}
.ITopic:hover {
    filter: brightness(1.1);
    cursor: pointer;
}
.ITopic img {
    height: 50px;
    border-radius: 50px;
    box-shadow: 0 1px 4px rgb(0 0 0 / 40%);
    
    /* for inset shadow */
    z-index: -1;
    position: relative;
}

.IContent {
    margin-top: 40px;
}

@media (max-width: 768px) {
  .IHeader {
    height: 120px;
  }
  .IHeader h1 {
    padding-top: 2.5rem;
  }
  .IHeader .IFace {
    top: 70px;
    width: 110px;
    height: 110px;
  }
  .ITopics {
    padding-left: 180px;
  }
  .ITopic {
    --bs-gutter-x: 1.5rem;
  }
  .ITopic img {
    height: 40px;
  }
  .IContent {
    margin-top: 25px;
  }
}

.lead h1, h2 {
    margin-bottom: 0px;
}