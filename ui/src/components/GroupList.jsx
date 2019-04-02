import * as React from "react";
import { List, ListItem, Divider } from "material-ui";
import FontAwesomeIcon from "@fortawesome/react-fontawesome";
import { faCubes, faChartLine }	from "@fortawesome/fontawesome-free-solid";

const GroupList = (props) => {
	return (
		<List id={props.id} style={{"overflowY": "auto", height: props.height}}>
			{props.data.map(function (fragility) {
				if (fragility.is3dPlot){
					return (<div key={fragility.id}>
						<ListItem onClick={() => props.onClick(fragility)}
								  disabled={fragility === props.selectedFragility}>
							<FontAwesomeIcon icon={faCubes} style={{"display": "inline", marginRight: "5px"}}/>
							{getTitle(fragility)}
						</ListItem>
						<Divider />
					</div>);

				}
				else{
					return (<div key={fragility.id}>
						<ListItem onClick={() => props.onClick(fragility)}
								  disabled={fragility === props.selectedFragility}>
							<FontAwesomeIcon icon={faChartLine} style={{"display": "inline", marginRight: "5px"}}/>
							{getTitle(fragility)}
						</ListItem>
						<Divider/>
					</div>);
				}
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
