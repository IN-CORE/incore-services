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
            <th>Titles</th>
            <th>Description</th>
            <th>Datatype</th>
            <th>Unit</th>
            <th>Required</th>
        </tr>
        <#list columns as column>
            <tr>
                <td>${column.name}</td>
                <td>${column.titles}</td>
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
