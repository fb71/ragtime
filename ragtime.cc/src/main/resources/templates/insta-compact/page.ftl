<#--
  insta-compact
-->
<#import "commons.ftl" as c>
<#import "insta#page.ftl" as super>

<#--
  The main page layout. 
-->
<#macro layout title pageClass="IHome" hideTopicBio=false>
<html>
<head>
    <title>${title} - ${config.page.title}</title>
    <meta charset="iso-8859-1">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    
    <link href="common/bs5.3.3/bootstrap.min.css" rel="stylesheet"/>
    <#-- Template styles -->
    <link href="css/common.css" rel="stylesheet"/>
    <link href="topic/css/topic.css" rel="stylesheet"/>
    <link href="insta/css/insta.css" rel="stylesheet"/>
    <link href="insta-compact/css/insta-compact.css" rel="stylesheet"/>
    <#-- TemplateConfigEntity styles-->
    <link href="config.css" rel="stylesheet"/>

    <script src="common/bs5.3.3/bootstrap.bundle.min.js" type="text/javascript"></script>
    <script src="common/js/common.js" type="text/javascript"></script>
</head>

<body class="${pageClass}">
    <#-- Page header -->
    <header class="IHeader" <#-- style="background-image: url(<@c.mediaUrl name="Raumfoto.jpg"/>)" -->>
        <div class="IBgImage container" style="height:100%;
            <#if config.bannerImage??>
                background-image: url(media/${config.bannerImage.id});
            </#if>
            ">
            <@c.editable msg="page.title">
                <h1>${config.page.title}</h1>
                <h4>${config.page.title2}</h4>
            </@c.editable>
            <#-- Face -->
            <div class="IFace">
                <@c.editable msg="page.title">
                    <#if config.leadImage??>
                        <img class="IFaceBordered" src="media/${config.leadImage.id}?w=180&h=180"></img>
                    </#if>
                </@c.editable>
            </div>
        </div>
    </header>

    <#-- Topics -->
    <@super.topicsSection/>

    <#-- TopicBio -->
    <#if !hideTopicBio>
        <@super.topicBioSection/>
    </#if>
    
    <#-- Content -->
    <@super.contentSection>
        <#nested>        
    </@super.contentSection>

    <#-- Footer -->
    <@super.footerSection/>
</body>
</html>
</#macro>
