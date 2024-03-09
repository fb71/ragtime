<#--
  The main page layout. 
-->
<#macro layout title pageclass>
<html>
<head>
    <title>${title} - ${config.page.title}</title>
    <meta charset="iso-8859-1">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="bs5.3.3/bootstrap.min.css" rel="stylesheet"/>
    <#-- Styles specific to the common template -->
    <link href="css/common.css" rel="stylesheet"/>
    <#-- Styles from TemplateConfigEntity -->
    <link href="config.css" rel="stylesheet"/>
    <script src="bs5.3.3/bootstrap.bundle.min.js" type="text/javascript"></script>
</head>

<body class="${pageclass}">
    <#-- Page header with logo and tagline-->
    <header class="CHeader py-3 border-bottom">
        <div class="container">
            <div class="text-center my-5">
                <h1 class="fw-bolder">${config.page.title}</h1>
                <p class="lead mb-0">${config.page.title2}</p>
            </div>
        </div>
    </header>
    
    <#-- Responsive navbar-->
    <nav class="CNavbar navbar navbar-expand-lg mb-4">
        <div class="container">
            <a class="navbar-brand" href="#!"></a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarSupportedContent">
                <ul class="navbar-nav ms-auto mb-2 mb-lg-0">
                    <#list config.navItems as item>
                        <li class="nav-item"><a class="nav-link" href="${item.href}">${item.title}</a></li>
                    </#list>

                    <#-- li class="nav-item"><a class="nav-link" href="frontpage">Home</a></li>
                    <li class="nav-item"><a class="nav-link" href="kontakt">Kontakt</a></li>
                    <li class="nav-item"><a class="nav-link" href="datenschutz">Datenschutz</a></li>
                    <li class="nav-item"><a class="nav-link active" aria-current="page" href="impressum">Impressum</a></li-->
                </ul>
            </div>
        </div>
    </nav>
    
    <div class="CContent container">
    <#nested>
    </div>

    <#-- Footer-->
    <footer class="CFooter py-2">
        <div class="container">
            <p class="m-0 text-center small">
            ${config.page.footer}&nbsp;&nbsp;|&nbsp;&nbsp;Powered by <a href="http://publico.de">Wizard & Crew</a>
            </p>
        </div>
    </footer>    
</body>
</html>
</#macro>