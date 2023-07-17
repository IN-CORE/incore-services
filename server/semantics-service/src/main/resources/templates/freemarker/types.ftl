<!DOCTYPE html>
<head>
    <title>Type Description</title>
    <style>
        table {
            font-family: arial, sans-serif;
            border-collapse: collapse;
            width: 100%;
        }

        td, th {
            border: 1px solid #dddddd;
            text-align: left;
            padding: 8px;
        }

        tr:nth-child(even) {
            background-color: #dddddd;
        }

        div {
            padding-top: 20px;
            padding-right: 220px;
            padding-bottom: 20px;
            padding-left: 20px;
        }
    </style>
</head>
<body>
<div>
    <h1>${title}</h1>
    <p><strong>Description</strong></p>
    <p>${description}</p>
    <p>Each schema defines the expected attributes for a dataset definition. The attributes for the <b>${title}</b>
        schema are listed in the table below.</p>

    <table>
        <tr>
            <th>Name</th>
            <th>Title</th>
            <th>Description</th>
            <th>Datatype</th>
            <th>Required</th>
            <th>Unit</th>
        </tr>
<#--        <#list columns as column>-->
<#--            <!-- Access column properties using ${column.property} syntax &ndash;&gt;-->
<#--            <tr>-->
<#--                <td>${column.name}</td>-->
<#--                <td>${column.titles}</td>-->
<#--                <td>${column["dc:description"]}</td>-->
<#--                <td>${column.datatype}</td>-->
<#--                <td>${column.required}</td>-->
<#--                <td>${column["qudt:unit"]}</td>-->
<#--            </tr>-->
<#--        </#list>-->
    </table>
</div>
</body>
</html>
