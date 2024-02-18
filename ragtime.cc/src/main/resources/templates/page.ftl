<#--
  The main page layout. 
-->
<#macro layout title>
<html>
<head>
    <title>${title}</title>
    <meta charset="iso-8859-1">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous">
    <link href="../css/template-common.css" rel="stylesheet">
</head>

<body>
    <#-- Page header with logo and tagline-->
    <header class="py-3 bg-light border-bottom">
        <div class="container">
            <div class="text-center my-5">
                <h1 class="fw-bolder">${config.page.title}</h1>
                <p class="lead mb-0">${config.page.title2}</p>
            </div>
        </div>
    </header>
    
    <#-- Responsive navbar-->
    <nav class="navbar navbar-expand-lg border-bottom mb-4">
        <div class="container">
            <a class="navbar-brand" href="#!"></a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation"><span class="navbar-toggler-icon"></span></button>
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
    
    <div class="container">
    <#nested>
    </div>

    <#-- Footer-->
    <footer class="py-2 bg-light">
        <div class="container">
            <p class="m-0 text-center small">
            ${config.page.footer}&nbsp;&nbsp;|&nbsp;&nbsp;Powered by <a href="">Wizard & Crew</a>
            </p>
        </div>
    </footer>    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js" integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL" crossorigin="anonymous"></script>
</body>
</html>
</#macro>