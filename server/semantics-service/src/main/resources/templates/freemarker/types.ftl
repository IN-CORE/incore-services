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
    <p>Each schema defines the expected attributes for a dataset definition. The attributes for the <b>${title}</b>
        schema are listed in the table below.</p>

    <table>
        <tr>
            <th>Parameter</th>
            <th>Value</th>
        </tr>
        <#list items?keys as key>
            <tr>
                <td>${key}</td>
                <td>${items[key]} </td>
            </tr>
        </#list>
    </table>
</div>
</body>
</html>
