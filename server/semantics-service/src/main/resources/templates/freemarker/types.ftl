<!DOCTYPE html>
<head>
    <title>Type Description</title>
    <link rel="stylesheet"
          href="https://cdn.jsdelivr.net/npm/bootstrap@4.0.0/dist/css/bootstrap.min.css"
          integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm"
          crossorigin="anonymous">
    <style>
        body {
            font-size: 12px;
            line-height: 1.4em;
            min-width: 230px;
            margin: 0 auto;
            -webkit-font-smoothing: antialiased;
            font-weight: 300;
            height:100%;
        }
        div {
            padding: 2em 5em;
        }
    </style>
</head>
<body>
<div>
    <h1 class="display-4">${title}</h1>
    <p class="lead">${description}</p>
    <p class="text-muted">Each schema defines the expected attributes for a dataset definition.
        The attributes for the <b>${title}</b>
        schema are listed in the table below.</p>
    <a href="/semantics/api/types/${url}/template" class="lead">Download Template</a>
    <table class="table" style="margin:2em auto;">
        <tr style="background: #eee;">
            <th scope="col">Name</th>
            <th scope="col">Description</th>
            <th scope="col">Datatype</th>
            <th scope="col">Unit</th>
            <th scope="col">Required</th>
        </tr>
        <#list columns as column>
            <tr>
                <th scope="row">${column.name}</th>
                <td>${column.description}</td>
                <td>${column.datatype}</td>
                <td>${column.unit}</td>
                <td>${column.required}</td>
            </tr>
        </#list>
    </table>
</div>
</body>
</html>
