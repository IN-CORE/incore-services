import React, {Component} from "react";
import Table from "./Table";
import Map from "./Map";
import Notification from "../components/Notification";
import {
	GridList,
	GridTile,
	SelectField,
	MenuItem,
	List,
	ListItem,
	Divider,
	TextField,
	IconButton,
	RaisedButton,
	Checkbox
} from "material-ui";
import FontAwesomeIcon from "@fortawesome/react-fontawesome";
import {
	faTable,
	faFileAlt,
	faMap,
	faExchangeAlt,
	faChartArea,
	faShareAlt,
	faQuestionCircle
} from "@fortawesome/fontawesome-free-solid";
import ActionSearch from "material-ui/svg-icons/action/search";
import csv from "csv";
import config from "../app.config";
import {getHeader, getUsername} from "../actions";
import {browserHistory} from "react-router";
import loaderGif from "../public/loader.gif";

String.prototype.capitalize = function () {
	return this.charAt(0).toUpperCase() + this.slice(1);
};

class DataViewer extends Component {

	constructor(props) {
		super(props);
		this.state = {
			typeIndex: 0,
			selectedDataset: "",
			selectedDatasetFormat: "",
			fileData: "",
			fileExtension: "",
			currentUser: "",
			authError: false,
			authLocationFrom: null
		};
		this.changeDatasetType = this.changeDatasetType.bind(this);
		this.onClickDataset = this.onClickDataset.bind(this);
		this.searchDatasets = this.searchDatasets.bind(this);
		this.handleKeyPressed = this.handleKeyPressed.bind(this);
		this.onClickFileDescriptor = this.onClickFileDescriptor.bind(this);
		this.updateCheckCreatorFilter = this.updateCheckCreatorFilter.bind(this);
		this.exportJson = this.exportJson.bind(this);
		this.downloadDataset = this.downloadDataset.bind(this);
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
			this.props.getAllDatasets();
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

	changeDatasetType(event, index, value) {
		this.setState({
			typeIndex: value,
			selectedDataset: "",
			fileData: "",
			fileExtension: "",
			selectedDatasetFormat: "",
			searchText: ""
		});
	}

	onClickDataset(datasetId) {
		const dataset = this.props.datasets.find(dataset => dataset.id === datasetId);
		this.setState({
			selectedDataset: datasetId,
			selectedDatasetFormat: dataset.format,
			fileData: "",
			fileExtension: ""
		});

	}

	async searchDatasets() {
		let searchText = this.refs.searchBox.getValue();
		this.setState({
			searchText: searchText,
			selectedDataset: "",
			fileData: "",
			fileExtension: "",
			selectedDatasetFormat: ""
		});
	}

	async handleKeyPressed(event) {
		if (event.charCode === 13) { // enter
			event.preventDefault();
			await this.searchDatasets();
		}
	}

	async onClickFileDescriptor(selected_dataset_id, file_descriptor_id, file_name) {
		const url = `${config.dataServiceBase }data/api/files/${  file_descriptor_id  }/blob`;

		let response = await fetch(url, {method: "GET", mode: "cors", headers: getHeader()});

		if (response.ok) {
			let text = await response.text();
			this.setState({
				fileData: text.split("\n"),
				fileExtension: file_name.split(".").slice(-1).pop(),
				authError: false,
			});

		}
		else if (response.status === 403) {
			this.setState({
				fileData: [],
				fileExtension: null,
				authError: true
			});
		}
		else {
			this.setState({
				fileData: [],
				fileExtension: null,
				authError: false
			});
		}
	}

	async downloadDataset() {
		let datasetId = this.state.selectedDataset;
		let filename = `${datasetId}.zip`;
		let url = `${config.dataService}/${datasetId}/blob`;

		let response = await fetch(url, {method: "GET", mode: "cors", headers: await getHeader()});

		if (response.ok) {
			let blob = await response.blob();
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
		else if (response.status === 403) {
			this.setState({
				authError: true
			});
		}
		else {
			this.setState({
				authError: false
			});
		}

	}

	async exportJson() {
		const selected_dataset = this.props.datasets.find(dataset => dataset.id === this.state.selectedDataset);
		let datasetJSON = JSON.stringify(selected_dataset, null, 4);
		let blob = new Blob([datasetJSON], {type: "application/json"});

		const filename = `${selected_dataset.id}.json`;

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

	async updateCheckCreatorFilter(event) {
		let currentUser = getUsername();
		if (event.target.checked) {
			this.setState({
				currentUser: currentUser,
				selectedDataset: "",
				fileData: "",
				fileExtension: "",
				selectedDatasetFormat: ""
			});
		}
		else {
			this.setState({
				currentUser: "",
				selectedDataset: "",
				fileData: "",
				fileExtension: "",
				selectedDatasetFormat: ""
			});
		}
	}

	render() {
		let unique_types = [];
		if (this.props.datasets.length > 0) {
			const dataset_all_types = this.props.datasets.map(dataset =>
				dataset.dataType
			);
			unique_types = Array.from(new Set(dataset_all_types));
			unique_types.splice(0, 0, "All");
		}

		let dataset_types = "";
		if (unique_types.length > 0) {
			const type_menu_items = unique_types.map((type, index) =>
				<MenuItem value={index} primaryText={type} key={type}/>
			);
			dataset_types = (<SelectField fullWidth={true}
										  floatingLabelText="Dataset Type"
										  value={this.state.typeIndex}
										  onChange={this.changeDatasetType}
			>
				{type_menu_items}
			</SelectField>);
		}
		let datasets_to_display = this.props.datasets;
		if (this.state.typeIndex) {
			datasets_to_display = this.props.datasets.filter(dataset => dataset.dataType === unique_types[this.state.typeIndex]);
		}
		if (this.state.currentUser) {
			datasets_to_display = datasets_to_display.filter(dataset => dataset.spaces.indexOf(this.state.currentUser) > -1);
		}
		if (this.state.searchText) {
			datasets_to_display = datasets_to_display.filter(dataset => dataset.title.toLowerCase()
				.indexOf(this.state.searchText.toLowerCase()) > -1);
		}

		// list items
		let list_items = "";
		if (datasets_to_display.length > 0) {
			list_items = datasets_to_display.map((dataset) => {
				if (dataset.format === "table") {
					return (<div key={dataset.id}>
						<ListItem onClick={() => this.onClickDataset(dataset.id)}
								  disabled={dataset.id === this.state.selectedDataset}>
							<FontAwesomeIcon icon={faTable} style={{"display": "inline", marginRight: "5px"}}/>
							{`${dataset.title  } - ${  dataset.creator.capitalize()}`}
						</ListItem>
						<Divider/>
					</div>);
				}
				else if (dataset.format === "textFiles") {
					return (<div key={dataset.id}>
						<ListItem onClick={() => this.onClickDataset(dataset.id)}
								  disabled={dataset.id === this.state.selectedDataset}>
							<FontAwesomeIcon icon={faFileAlt}
											 style={{"display": "inline", marginRight: "5px"}}/>
							{`${dataset.title  } - ${  dataset.creator.capitalize()}`}
						</ListItem>
						<Divider/>
					</div>);
				}
				else if (dataset.format === "shapefile") {
					return (<div key={dataset.id}>
						<ListItem onClick={() => this.onClickDataset(dataset.id)}
								  disabled={dataset.id === this.state.selectedDataset}>
							<FontAwesomeIcon icon={faMap}
											 style={{"display": "inline", marginRight: "5px"}}/>
							{`${dataset.title  } - ${  dataset.creator.capitalize()}`}
						</ListItem>
						<Divider/>
					</div>);
				}
				else if (dataset.format === "mapping") {
					return (<div key={dataset.id}>
						<ListItem onClick={() => this.onClickDataset(dataset.id)}
								  disabled={dataset.id === this.state.selectedDataset}>
							<FontAwesomeIcon icon={faExchangeAlt}
											 style={{"display": "inline", marginRight: "5px"}}/>
							{`${dataset.title  } - ${  dataset.creator.capitalize()}`}
						</ListItem>
						<Divider/>
					</div>);
				}
				else if (dataset.format === "fragility") {
					return (<div key={dataset.id}>
						<ListItem onClick={() => this.onClickDataset(dataset.id)}
								  disabled={dataset.id === this.state.selectedDataset}>
							<FontAwesomeIcon icon={faChartArea}
											 style={{"display": "inline", marginRight: "5px"}}/>
							{`${dataset.title  } - ${  dataset.creator.capitalize()}`}
						</ListItem>
						<Divider/>
					</div>);
				}
				else if (dataset.format === "Network") {
					return (<div key={dataset.id}>
						<ListItem onClick={() => this.onClickDataset(dataset.id)}
								  disabled={dataset.id === this.state.selectedDataset}>
							<FontAwesomeIcon icon={faShareAlt} style={{
								"display": "inline",
								marginRight: "5px"
							}}/>
							{`${dataset.title  } - ${  dataset.creator.capitalize()}`}
						</ListItem>
						<Divider/>
					</div>);
				}
				else {
					return (<div key={dataset.id}>
						<ListItem onClick={() => this.onClickDataset(dataset.id)}>
							<FontAwesomeIcon icon={faQuestionCircle} style={{
								"display": "inline",
								marginRight: "5px"
							}}/>
							{`${dataset.title  } - ${  dataset.creator.capitalize()}`}
						</ListItem>
						<Divider/>
					</div>);
				}
			});
		}
		const filtered_datasets = (<div style={{overflow: "auto", height: "600px", margin: "0 20px"}}>
			<List style={{"overflowY": "auto"}}>
				{list_items}
			</List>
		</div>);

		// after selected an item
		let file_descriptors = "";
		let file_contents = "";
		let right_column = "";
		if (this.state.selectedDataset) {
			const selected_dataset = this.props.datasets.find(dataset => dataset.id === this.state.selectedDataset);

			// file description
			file_descriptors =
				selected_dataset.fileDescriptors.map(file_descriptor =>
					<div key={file_descriptor.id}>
						<ListItem onClick={() => this.onClickFileDescriptor(selected_dataset.id, file_descriptor.id,
							file_descriptor.filename)} primaryText={file_descriptor.filename} key={file_descriptor.id}/>
						<Divider/>
					</div>
				);

			// file contents
			if (this.state.fileExtension && this.state.fileData && this.state.fileExtension === "csv") {
				let data = this.state.fileData.map((data) => data.split(","));
				file_contents = (
					<div style={{overflow: "auto", height: "300px"}}>
						<h2>File Content Preview</h2>
						<Table container="data_container" data={data.slice(2, 8)} colHeaders={data[0]}
							   rowHeaders={false}/>;
					</div>
				);
			}
			else if (this.state.fileExtension === "xml" || this.state.fileExtension === "txt") {
				file_contents = (
					<div style={{overflow: "auto", height: "300px"}}>
						<h2>File Content Preview</h2>
						<p>{this.state.fileData}</p>
					</div>
				);
			}

			// button groups
			let downloads = (
				<div style={{marginLeft: "auto", marginBottom: "10px"}}>
					<RaisedButton primary={true} style={{display: "inline-block"}}
								  label="Download Metadata" labelPosition="before" onClick={this.exportJson}
								  style={{marginRight: "10px"}}/>
					<RaisedButton primary={true} style={{display: "inline-block"}} label="Download Dataset"
								  labelPosition="before" onClick={this.downloadDataset}/>
				</div>
			);

			// right column
			if (this.state.selectedDatasetFormat === "shapefile") {
				right_column =
					(<div>
						<h2>Preview</h2>
						{downloads}
						<Map datasetId={this.state.selectedDataset}
							 boundingBox={selected_dataset.boundingBox}/>
					</div>);
			} else if (file_descriptors.length > 0) {
				right_column = (
					<div>
						<h2>File Descriptors</h2>
						{downloads}
						{/*list of files*/}
						<List style={{"overflowY": "auto", height: "200px"}}>
							{file_descriptors}
						</List>
					</div>
				);
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
						<h2>Data Viewer</h2>
					</div>
					<GridList cols={12} cellHeight="auto">
						<GridTile cols={3}>
							{dataset_types}
						</GridTile>
						<GridTile cols={3}>
							<Checkbox
								label="Show Only My Datasets"
								onCheck={this.updateCheckCreatorFilter}
								style={{top: "37px", marginLeft: "20px"}}
							/>
						</GridTile>
						<GridTile cols={6} style={{float: "right"}}>
							<TextField ref="searchBox" hintText="Search Datasets" onKeyPress={this.handleKeyPressed}/>
							<IconButton iconStyle={{position: "absolute", left: 0, bottom: 5, width: 30, height: 30}}
										onClick={this.searchDatasets}>
								<ActionSearch/>
							</IconButton>
						</GridTile>
					</GridList>
					<GridList cols={12} style={{paddingTop: "10px"}} cellHeight="auto">
						<GridTile cols={5}>
							<h2> Datasets </h2>
							{
								this.props.datasets.length === 0 ?
									<img src={loaderGif} style={{margin: "80px auto", display: "block"}}/>
									:
									null
							}
							{filtered_datasets}
						</GridTile>
						<GridTile cols={7}>
							{right_column}
							{file_contents}
						</GridTile>
					</GridList>
				</div>
			);
		}
	}

}

export default DataViewer;
