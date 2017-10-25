import * as React from "react";
import { List, ListItem, Divider, RaisedButton } from "material-ui";

const GroupList = (props) => {
	return (
		<List id={props.id} style={{"overflowY": "auto", height: props.height}}>
			{props.data.map(function (dataItem) {
				return (<div key={dataItem.id}>
							<ListItem onClick={() => props.onClick(dataItem)}>
								{getTitle(dataItem)}
							</ListItem>
							<Divider />
						</div>);
			})}
		</List>
	);
};

function getTitle(fragility) {
	let title = fragility.authors.join(", ");

	if (fragility.paperReference !== null) {
		title += ` (${fragility.paperReference.yearPublished})`;
	}

	title += ` - ${fragility.legacyId}`;

	return title;
}

export default GroupList;
