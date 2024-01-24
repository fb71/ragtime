<html>
<head>
    <title>Articles Template</title>
    <style>
    html * {
       color: #303030;
       font-family: Arial;
    }
    </style>
</head>

<body>
   <h1>Artikel</h1>
   
   <ul>
    <#list articles as article>
      <li>[${article_index + 1}] ${article.title}<br/>${article.content}</li>
    </#list>
  </ul>
</body>
</html>