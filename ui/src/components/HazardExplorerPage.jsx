import React, {Component} from "react";
import {getHeader} from "../actions";
import {browserHistory} from "react-router";
import {
	GridList, GridTile, List,
	ListItem, Divider, TextField, IconButton,
	SelectField, MenuItem, RaisedButton
} from "material-ui";
import ActionSearch from "material-ui/svg-icons/action/search";
import Map from "./Map";
import Notification from "./Notification";
import NestedInfoTable from "./NestedInfoTable";
import config from "../app.config";

const subtype_list = {
	earthquakes: ["All", "Model", "Dataset"],
	tornadoes: ["All", "Mean Width Tornado", "Mean Length Width Angle Tornado", "Random Length Width Angle Tornado",
		"Random Width Tornado", "Random Angle Tornado"],
	hurricaneWindfields: ["All"],
	tsunamis: ["All", "Model", "Dataset"]
};

const redundant_prop = ["description", "privileges"];

class HazardExplorerPage extends Component {

	constructor(props) {
		super(props);
		this.state = {
			type: "earthquakes",
			selectedHazard: "",
			selectedHazardDatasetId: "",
			boundingBox: [],
			selectedSubtype: "",
			searchText: "",
			authError: false,
			authLocationFrom: null,
			expanded: true
		};
		this.changeHazardType = this.changeHazardType.bind(this);
		this.onClickHazard = this.onClickHazard.bind(this);
		this.onClickHazardDataset = this.onClickHazardDataset.bind(this);
		this.changeHazardSubType = this.changeHazardSubType.bind(this);
		this.searchHazards = this.searchHazards.bind(this);
		this.handleKeyPressed = this.handleKeyPressed.bind(this);
		this.exportJson = this.exportJson.bind(this);
	}

	componentWillMount() {
		// check if logged in
		let user = sessionStorage.getItem("user");
		let auth = sessionStorage.getItem("auth");
		let location = sessionStorage.getItem("locationFrom");

		// logged in
		if (user !== undefined && user !== "" && user !== null
			&& auth !== undefined && auth !== "" && auth !== null) {

			this.setState({
				authError: false
			});

			// fetch datasets
			this.props.getAllHazards(this.state.type);

			// subtype default to the first one within that type
			this.setState({selectedSubtype: subtype_list[this.state.type][0]});

		}

		// not logged in
		else {
			this.setState({
				authError: true,
				authLocationFrom: location
			});
		}
	}

	componentWillReceiveProps(nextProps) {
		this.setState({
			authError: nextProps.authError,
			authLocationFrom: nextProps.locationFrom
		});
	}

	changeHazardType(event, index, value) {
		this.setState({
			type: value, selectedSubtype: subtype_list[value][0],
			selectedHazard: "", selectedHazardDatasetId: "", searchText: ""
		});
		this.props.getAllHazards(value);
	}

	changeHazardSubType(event, index, value) {
		this.setState({
			selectedSubtype: value, selectedHazard: "",
			selectedHazardDatasetId: "", searchText: ""
		});
	}

	onClickHazard(hazardId) {
		this.setState({selectedHazard: hazardId, selectedHazardDatasetId: ""});
	}

	async onClickHazardDataset(hazardDatasetId) {
		// query data services to:
		// 1. verify that dataset exists
		// 2. get the bounding box information

		// check if the same hazard dataset has been clicked
		// toggle
		if (this.state.selectedHazardDatasetId === hazardDatasetId){
			this.setState (state =>({
				expanded: !state.expanded
			}));
		}
		// else always expand details
		else{
			this.setState({expanded: true});

			const url = `${config.dataServiceBase}data/api/datasets/${hazardDatasetId}`;
			let response = await fetch(url, {method: "GET", mode: "cors", headers: getHeader()});
			if (response.ok) {
				let selectedHazardDataset = await response.json();
				this.setState( state => ({
					selectedHazardDatasetId: selectedHazardDataset.id,
					boundingBox: selectedHazardDataset.boundingBox,
					authError: false,
				}));
			}
			else if (response.status === 403) {
				this.setState({
					selectedHazardDatasetId: "",
					boundingBox: [],
					authError: true
				});
			}
			else {
				this.setState({
					selectedHazardDatasetId: "",
					boundingBox: [],
					authError: false
				});
			}
		}
	}

	async searchHazards() {
		let searchText = this.refs.searchBox.getValue();
		this.setState({searchText: searchText, selectedHazard: "", selectedHazardDatasetId: ""});
	}

	async handleKeyPressed(event) {
		if (event.charCode === 13) { // enter
			event.preventDefault();
			await this.searchHazards();
		}
	}

	exportJson() {
		let selected_hazard = this.props.hazards.find(hazard => hazard.id === this.state.selectedHazard);
		let hazardJson = JSON.stringify(selected_hazard, null, 4);
		let blob = new Blob([hazardJson], {type: "application/json"});
		const filename = `${this.state.selectedHazard}.json`;

		if (window.navigator.msSaveOrOpenBlob) {
			window.navigator.msSaveBlob(blob, filename);
		} else {
			let anchor = window.document.createElement("a");
			anchor.href = window.URL.createObjectURL(blob);
			anchor.download = filename;
			document.body.appendChild(anchor);
			anchor.click();
			document.body.removeChild(anchor);
		}
	}


	render() {
		let hazards_list = this.props.hazards;
		let right_column = "";
		let map = "";
		let hazards_list_display = "";

		//filter hazards based on their subtype
		let filtered_hazards_list = [];
		if (this.state.selectedSubtype.toLowerCase() === "all") {
			filtered_hazards_list = hazards_list;
		}
		else {
			if (this.state.type === "earthquakes") {
				filtered_hazards_list = hazards_list.filter((hazard) => {
					return hazard.eqType.toLowerCase() === this.state.selectedSubtype.toLowerCase();
				});
			}
			else if (this.state.type === "tornadoes") {
				// tornado type name example MeanWidthAngleTornado
				filtered_hazards_list = hazards_list.filter((hazard) => {
					return hazard.tornadoModel.toLowerCase() === this.state.selectedSubtype.toLowerCase().replace(/\s+/g, "");
				});
			}
			else if (this.state.type === "tsunamis") {
				filtered_hazards_list = hazards_list.filter((hazard) => {
					return hazard.tsunamiType.toLowerCase() === this.state.selectedSubtype.toLowerCase();
				});
			}
			else if (this.state.type === "hurricane") {
				filtered_hazards_list = hazards_list;
			}
		}

		// search throught the filtered hazards list
		let searched_hazards_list = (filtered_hazards_list.length > 0
			&& "description" in filtered_hazards_list[0] && "name" in filtered_hazards_list[0]) ?
			filtered_hazards_list.filter(hazard => hazard.description.toLowerCase()
					.indexOf(this.state.searchText.toLowerCase()) > -1
				|| hazard.name.toLowerCase().indexOf(this.state.searchText.toLowerCase()) > -1)
			: filtered_hazards_list;

		if (searched_hazards_list.length > 0) {

			// rendering filtered hazards list
			hazards_list_display = (
				<List style={{"overflowY": "auto"}}>
					{
						searched_hazards_list.map((hazard) => {
							// work around to highlight (disabled) the selected item; selected for some reason doesn't work
							return (
								<div key={hazard.id}>
									<ListItem onClick={() => this.onClickHazard(hazard.id)} key={hazard.id}
											  primaryText={(hazard.name) ? `${hazard.name }` : `${hazard.id}`}
											  disabled={hazard.id === this.state.selectedHazard}/>
									<Divider/>
								</div>);
						})
					}
				</List>);

			// rendering description
			if (this.state.selectedHazard !== "") {
				const selected_hazard = this.props.hazards.find(hazard => hazard.id === this.state.selectedHazard);

				// do not render redundant props to save space
				let selected_hazard_detail = {};
				for (let item in selected_hazard) {
					if (redundant_prop.indexOf(item) === -1) {
						selected_hazard_detail[item] = selected_hazard[item];
					}
				}

				// info table
				let table = (<NestedInfoTable data={selected_hazard_detail}
											  selectedHazardDataset={this.state.selectedHazardDatasetId}
											  expanded={this.state.expanded}
											  onClick={this.onClickHazardDataset}/>);

				// right column
				right_column = (
					<div>
						<RaisedButton primary={true} style={{display: "inline-block"}} label="Download Metadata"
									  onClick={this.exportJson}/>
						<List style={{overflow: "auto", height: "300px", margin: "20px 20px"}}>
							<div key={`${selected_hazard.id} - "description"`}>
								<h4>{selected_hazard.description}</h4>
								{table}
							</div>
						</List>
					</div>);

				// rendering map previews
				if (this.state.selectedHazardDatasetId !== "") map = (
					<Map datasetId={this.state.selectedHazardDatasetId} boundingBox={this.state.boundingBox}/>);
			}
		}

		if (this.state.authError) {
			if (this.state.authLocationFrom !== undefined
				&& this.state.authLocationFrom !== null
				&& this.state.authLocationFrom.length > 0) {
				return (<Notification/>);
			}
			else {
				browserHistory.push(`${config.baseUrl}`);
				return null;
			}
		}
		else {
			return (
				<div style={{padding: "20px"}}>
					<div style={{display: "flex"}}>
						<h2>Hazard Viewer</h2>
					</div>

					<GridList cols={12} cellHeight="auto">
						{/* select hazard type */}
						<GridTile cols={3}>
							<SelectField fullWidth={true} floatingLabelText="Hazard Type"
										 value={this.state.type}
										 onChange={this.changeHazardType}
							>
								<MenuItem value="earthquakes" primaryText="Earthquake" key="earthquakes"/>
								<MenuItem value="tornadoes" primaryText="Tornado" key="tornadoes"/>
								<MenuItem value="hurricaneWindfields" primaryText="Hurricane"
										  key="hurricaneWindfields"/>
								<MenuItem value="tsunamis" primaryText="Tsunami" key="tsunamis"/>
							</SelectField>
						</GridTile>

						{/* select subtype within each hazard */}
						<GridTile cols={3}>
							<SelectField fullWidth={true} floatingLabelText="Hazard SubType"
										 value={this.state.selectedSubtype}
										 onChange={this.changeHazardSubType}
							>
								{
									subtype_list[this.state.type].map((item) => {
										return (<MenuItem value={item} primaryText={item} key={item}/>);
									})
								}
							</SelectField>
						</GridTile>

						{/* search hazard based on name or description */}
						<GridTile cols={6} style={{float: "right"}}>
							<TextField ref="searchBox" hintText="Search Hazard"
									   onKeyPress={this.handleKeyPressed}/>
							<IconButton iconStyle={{position: "absolute", left: 0, bottom: 5, width: 30, height: 30}}
										onClick={this.searchHazards}>
								<ActionSearch/>
							</IconButton>
						</GridTile>
					</GridList>

					<GridList cols={12} style={{paddingTop: "12px"}} cellHeight="auto">
						<GridTile cols={5}>
							<h2>Hazards</h2>
							<div style={{overflow: "auto", height: "200px", margin: "0 20px"}}>
								{hazards_list_display}
							</div>
							<h2>Details</h2>
							<div>
								{right_column}
							</div>
						</GridTile>
						<GridTile cols={7} style={{overflow: "auto", height: "600px"}}>
							<h2>Preview</h2>
							{map}
						</GridTile>
					</GridList>
				</div>
			);
		}
	}
}

export default HazardExplorerPage;
