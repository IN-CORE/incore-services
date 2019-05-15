import React, {Component} from "react";
import {getHeader} from "../actions";
import {browserHistory} from "react-router";
import {
	Divider,
	GridList,
	GridTile,
	IconButton,
	List,
	ListItem,
	MenuItem,
	RaisedButton,
	SelectField,
	TextField
} from "material-ui";
import ActionSearch from "material-ui/svg-icons/action/search";
import Map from "./Map";
import Notification from "./Notification";
import NestedInfoTable from "./NestedInfoTable";
import config from "../app.config";

import FontAwesomeIcon from "@fortawesome/react-fontawesome";
import {faChevronLeft, faChevronRight,} from "@fortawesome/fontawesome-free-solid";

const redundant_prop = ["description", "privileges"];

class HazardExplorerPage extends Component {

	constructor(props) {
		super(props);
		this.state = {
			selectedHazardType: "earthquakes",
			selectedSpace:"All",
			selectedHazard: "",
			selectedHazardDatasetId: "",
			boundingBox: [],
			searchText: "",
			registeredSearchText:"",
			searching: false,
			authError: false,
			authLocationFrom: null,
			expanded: true,

			offset: 0,
			pageNumber: 1,
			dataPerPage: 50
		};
		this.changeHazardType = this.changeHazardType.bind(this);
		this.onClickHazard = this.onClickHazard.bind(this);
		this.onClickHazardDataset = this.onClickHazardDataset.bind(this);
		this.searchHazards = this.searchHazards.bind(this);
		this.handleKeyPressed = this.handleKeyPressed.bind(this);
		this.exportJson = this.exportJson.bind(this);
		this.handleSpaceSelection = this.handleSpaceSelection.bind(this);
		this.previous = this.previous.bind(this);
		this.next = this.next.bind(this);
		this.changeDataPerPage = this.changeDataPerPage.bind(this);
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
			}, function () {
				this.props.getAllSpaces();
				this.props.getAllHazards(this.state.selectedHazardType, this.state.selectedSpace, this.state.dataPerPage, this.state.offset);
			});
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
			pageNumber: 1,
			offset: 0,
			selectedHazardType: value,
			selectedHazard: "",
			selectedHazardDatasetId: "",
			searchText: "",
			registeredSearchText: "",
			searching: false,
		}, function () {
			this.props.getAllHazards(this.state.selectedHazardType, this.state.selectedSpace, this.state.dataPerPage, this.state.offset);
		});

	}

	handleSpaceSelection(event, index, value){
		this.setState({
			pageNumber: 1,
			offset: 0,
			selectedHazard: "",
			selectedHazardDatasetId: "",
			searchText: "",
			registeredSearchText: "",
			searching: false,
			selectedSpace: value
		}, function() {
			this.props.getAllHazards(this.state.selectedHazardType, this.state.selectedSpace, this.state.dataPerPage, this.state.offset);
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
		if (this.state.selectedHazardDatasetId === hazardDatasetId) {
			this.setState(state => ({
				expanded: !state.expanded
			}));
		}
		// else always expand details
		else {
			this.setState({expanded: true});

			const url = `${config.dataServiceBase}data/api/datasets/${hazardDatasetId}`;
			let response = await fetch(url, {method: "GET", mode: "cors", headers: getHeader()});
			if (response.ok) {
				let selectedHazardDataset = await response.json();
				this.setState(state => ({
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
		this.setState({
			registeredSearchText: this.refs.searchBox.getValue(),
			searching: true,
			selectedSpace: "All",
			selectedHazard: "",
			selectedHazardDatasetId: "",
			boundingBox: [],
			pageNumber: 1,
			offset: 0
		}, function(){
			this.props.searchAllHazards(this.state.selectedHazardType, this.state.registeredSearchText,
				this.state.dataPerPage, this.state.offset);
		});
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

	previous() {
		this.setState({
			offset: (this.state.pageNumber - 2) * this.state.dataPerPage,
			pageNumber: this.state.pageNumber - 1,
			selectedHazard: "",
			selectedHazardDatasetId: "",
			boundingBox: [],
		}, function () {
			if (this.state.registeredSearchText !== "" && this.state.searching) {
				this.props.searchAllHazards(this.state.selectedHazardType, this.state.registeredSearchText,
					this.state.dataPerPage, this.state.offset);
			}
			else {
				this.props.getAllHazards(this.state.selectedHazardType, this.state.selectedSpace, this.state.dataPerPage, this.state.offset);
			}
		});
	}

	next() {
		this.setState({
			offset: (this.state.pageNumber) * this.state.dataPerPage,
			pageNumber: this.state.pageNumber + 1,
			selectedHazard: "",
			selectedHazardDatasetId: "",
			boundingBox: [],
		}, function () {
			if (this.state.registeredSearchText !== "" && this.state.searching) {
				this.props.searchAllHazards(this.state.selectedHazardType, this.state.registeredSearchText,
					this.state.dataPerPage, this.state.offset);
			}
			else {
				this.props.getAllHazards(this.state.selectedHazardType, this.state.selectedSpace, this.state.dataPerPage, this.state.offset);
			}
		});
	}

	changeDataPerPage(event, index, value) {
		this.setState({
			pageNumber: 1,
			offset: 0,
			dataPerPage: value,
			selectedHazard: "",
			selectedHazardDatasetId: "",
			boundingBox: [],
		}, function () {
			if (this.state.registeredSearchText !== "" && this.state.searching) {
				this.props.searchAllHazards(this.state.selectedHazardType, this.state.registeredSearchText,
					this.state.dataPerPage, this.state.offset);
			}
			else {
				this.props.getAllHazards(this.state.selectedHazardType, this.state.selectedSpace, this.state.dataPerPage, this.state.offset);
			}
		});
	}

	render() {
		let hazards_list = this.props.hazards;
		let right_column = "";
		let map = "";
		let hazards_list_display = "";

		if (hazards_list.length > 0) {

			// rendering filtered hazards list
			hazards_list_display = (
				<List style={{"overflowY": "auto"}}>
					{
						hazards_list.map((hazard) => {
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

		let space_types = "";
		if (this.props.spaces.length > 0){
			const space_menu_items = this.props.spaces.map((space, index) =>
				<MenuItem value={space.metadata.name} primaryText={space.metadata.name}/>
			);
			space_types = (<SelectField floatingLabelText="Spaces"
										hintText="Spaces"
										value={this.state.selectedSpace}
										onChange={this.handleSpaceSelection}
										style={{maxWidth:"200px"}}>
				<MenuItem value="All" primaryText="All"/>
				{space_menu_items}
			</SelectField>);
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
			const data_per_page = (<SelectField floatingLabelText="Results per page"
												value={this.state.dataPerPage}
												onChange={this.changeDataPerPage}
												style={{maxWidth:"200px"}}>
				<MenuItem primaryText="15" value={15}/>
				<MenuItem primaryText="30" value={30}/>
				<MenuItem primaryText="50" value={50}/>
				<MenuItem primaryText="75" value={75}/>
				<MenuItem primaryText="100" value={100}/>
			</SelectField>);
			return (
				<div style={{padding: "20px"}}>
					<div style={{display: "flex"}}>
						<h2>Hazard Viewer</h2>
					</div>

					<GridList cols={12} cellHeight="auto">
						{/* select hazard type */}
						<GridTile cols={2}>
							<SelectField floatingLabelText="Hazard Type"
										 value={this.state.selectedHazardType}
										 onChange={this.changeHazardType}
										 style={{maxWidth:"200px"}}>
								<MenuItem value="earthquakes" primaryText="Earthquake" key="earthquakes"/>
								<MenuItem value="tornadoes" primaryText="Tornado" key="tornadoes"/>
								<MenuItem value="hurricaneWindfields" primaryText="Hurricane"
										  key="hurricaneWindfields"/>
								<MenuItem value="tsunamis" primaryText="Tsunami" key="tsunamis"/>
							</SelectField>
						</GridTile>

						{/*spaces*/}
						<GridTile cols={2}>
							{space_types}
						</GridTile>

						{/* set data per page to be shown */}
						<GridTile cols={2} style={{float: "left"}}>
							{data_per_page}
						</GridTile>

						{/* search hazard based on name or description */}
						<GridTile cols={6} style={{float: "right"}}>
							<TextField ref="searchBox" hintText="Search Hazard"
									   onKeyPress={this.handleKeyPressed}
									   value={this.state.searchText}
									   onChange={e=>{this.setState({searchText:e.target.value});}}/>
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

							{/*pagination*/}
							<div>
								<GridTile cols={6} style={{paddingTop: "5x", textAlign: "center"}} cellHeight="auto">
									<button disabled={this.state.pageNumber === 1} onClick={this.previous}>
										<FontAwesomeIcon icon={faChevronLeft} transform="grow-4"/> Prev
									</button>
									<button disabled={true}>{this.state.pageNumber}</button>
									<button disabled={this.props.hazards.length < this.state.dataPerPage}
											onClick={this.next}>
										Next <FontAwesomeIcon icon={faChevronRight} transform="grow-4"/></button>
								</GridTile>
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
