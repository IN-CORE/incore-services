import React, {Component} from "react";
import Table from "./Table";
import Map from "./Map";
import Notification from "../components/Notification";
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
	TextField,
} from "material-ui";
import FontAwesomeIcon from "@fortawesome/react-fontawesome";
import {
	faChartArea,
	faChevronLeft,
	faChevronRight,
	faExchangeAlt,
	faFileAlt,
	faMap,
	faQuestionCircle,
	faShareAlt,
	faTable,
} from "@fortawesome/fontawesome-free-solid";
import ActionSearch from "material-ui/svg-icons/action/search";
import config, { uniqueDataType } from "../app.config";
import {getHeader} from "../actions";
import {browserHistory} from "react-router";

String.prototype.capitalize = function () {
	return this.charAt(0).toUpperCase() + this.slice(1);
};

class DataViewer extends Component {

	constructor(props) {
		super(props);
		this.state = {
			selectedDataType: "All",
			selectedSpace:"All",
			selectedDataset: "",
			selectedDatasetFormat: "",
			fileData: "",
			fileExtension: "",
			searchText: "",
			registeredSearchText:"",
			searching: false,
			authError: false,
			authLocationFrom: null,
			offset: 0,
			pageNumber: 1,
			dataPerPage: 50,
		};
		this.changeDatasetType = this.changeDatasetType.bind(this);
		this.onClickDataset = this.onClickDataset.bind(this);
		this.searchDatasets = this.searchDatasets.bind(this);
		this.handleKeyPressed = this.handleKeyPressed.bind(this);
		this.onClickFileDescriptor = this.onClickFileDescriptor.bind(this);
		this.exportJson = this.exportJson.bind(this);
		this.downloadDataset = this.downloadDataset.bind(this);
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
				this.props.getAllDatasets(this.state.selectedDataType, this.state.selectedSpace, this.state.dataPerPage, this.state.offset);
				this.props.getAllSpaces();
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

	changeDatasetType(event, index, value) {
		this.setState({
			selectedDataType: value,
			pageNumber: 1,
			offset: 0,
			selectedDataset: "",
			selectedDatasetFormat: "",
			fileData: "",
			fileExtension: "",
			searchText: "",
			registeredSearchText: "",
			searching: false,
		}, function () {
			this.props.getAllDatasets(this.state.selectedDataType, this.state.selectedSpace, this.state.dataPerPage, this.state.offset);
		});
	}

	handleSpaceSelection(event, index, value){
		this.setState({
			selectedSpace: value,
			pageNumber: 1,
			offset: 0,
			selectedDataset: "",
			selectedDatasetFormat: "",
			fileData: "",
			fileExtension: "",
			searchText: "",
			registeredSearchText: "",
			searching: false,
		}, function () {
			this.props.getAllDatasets(this.state.selectedDataType, this.state.selectedSpace, this.state.dataPerPage, this.state.offset);
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
		this.setState({
			registeredSearchText: this.refs.searchBox.getValue(),
			searching: true,
			selectedDataset: "",
			fileData: "",
			fileExtension: "",
			selectedDatasetFormat: "",
			pageNumber: 1,
			offset: 0,
			selectedDataType: "All",
			selectedSpace: "All"
		}, function () {
			this.props.searchAllDatasets(this.state.registeredSearchText, this.state.dataPerPage, this.state.offset);
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

	previous() {
		this.setState({
			offset: (this.state.pageNumber - 2) * this.state.dataPerPage,
			pageNumber: this.state.pageNumber - 1,
			selectedDataset: "",
			selectedDatasetFormat: "",
			fileData: "",
			fileExtension: "",
		}, function () {
			if (this.state.registeredSearchText !== "" && this.state.searching){
				// change page on searchAllDatasets
				this.props.searchAllDatasets(this.state.registeredSearchText, this.state.dataPerPage, this.state.offset);
			}
			else{
				// change page on getAllDatasets
				this.props.getAllDatasets(this.state.selectedDataType, this.state.selectedSpace, this.state.dataPerPage, this.state.offset);
			}
		});
	}

	next() {
		this.setState({
			offset: (this.state.pageNumber) * this.state.dataPerPage,
			pageNumber: this.state.pageNumber + 1,
			selectedDataset: "",
			selectedDatasetFormat: "",
			fileData: "",
			fileExtension: "",
		}, function () {
			if (this.state.registeredSearchText !== "" && this.state.searching){
				// change page on searchAllDatasets
				this.props.searchAllDatasets(this.state.registeredSearchText, this.state.dataPerPage, this.state.offset);
			}
			else{
				// change page on getAllDatasets
				this.props.getAllDatasets(this.state.selectedDataType, this.state.selectedSpace, this.state.dataPerPage, this.state.offset);
			}
		});
	}

	changeDataPerPage(event, index, value) {
		this.setState({
			pageNumber: 1,
			offset: 0,
			dataPerPage: value,
			selectedDataset: "",
			selectedDatasetFormat: "",
			fileData: "",
			fileExtension: "",
		}, function () {
			if (this.state.registeredSearchText !== "" && this.state.searching){
				// change page on searchAllDatasets
				this.props.searchAllDatasets(this.state.registeredSearchText, this.state.dataPerPage, this.state.offset);
			}
			else{
				// change page on getAllDatasets
				this.props.getAllDatasets(this.state.selectedDataType, this.state.selectedSpace, this.state.dataPerPage, this.state.offset);
			}
		});
	}

	render() {
		const type_menu_items = uniqueDataType.map((type) =>
			<MenuItem value={type} primaryText={type} key={type}/>
		);
		let dataset_types = (<SelectField
			floatingLabelText="Dataset Type"
			value={this.state.selectedDataType}
			onChange={this.changeDatasetType}
			style={{maxWidth: "300px"}}>
			{type_menu_items}
		</SelectField>);

		let space_types = "";
		if (this.props.spaces.length > 0){
			const space_menu_items = this.props.spaces.map((space, index) =>
				<MenuItem value={space.metadata.name} primaryText={space.metadata.name}/>
			);
			space_types = (<SelectField fullWidth={true}
										floatingLabelText="Spaces"
										hintText="Spaces"
										value={this.state.selectedSpace}
										onChange={this.handleSpaceSelection}
										style={{maxWidth: "200px"}}>
				<MenuItem value="All" primaryText="All"/>
				{space_menu_items}
			</SelectField>);
		}

		// list items
		let list_items = "";
		if (this.props.datasets.length > 0) {
			list_items = this.props.datasets.map((dataset) => {
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
		const filtered_datasets = (<div style={{overflow: "auto", height: "45vh", margin: "0 20px"}}>
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
						<List style={{"overflowY": "auto", height: "100px"}}>
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
			const data_per_page = (<SelectField floatingLabelText="Results per page"
												value={this.state.dataPerPage}
												onChange={this.changeDataPerPage}
												style={{maxWidth: "200px"}}
			>
				<MenuItem primaryText="15" value={15}/>
				<MenuItem primaryText="30" value={30}/>
				<MenuItem primaryText="50" value={50}/>
				<MenuItem primaryText="75" value={75}/>
				<MenuItem primaryText="100" value={100}/>
			</SelectField>);

			return (
				<div style={{padding: "20px"}}>
					<div style={{display: "flex"}}>
						<h2>Data Viewer</h2>
					</div>
					<GridList cols={12} cellHeight="auto">
						{/*dataset types*/}
						<GridTile cols={3}>
							{dataset_types}
						</GridTile>

						{/*space types*/}
						<GridTile cols={2}>
							{space_types}
						</GridTile>

						{/*per page*/}
						<GridTile cols={2} style={{float: "left"}}>
							{data_per_page}
						</GridTile>

						{/*search box*/}
						<GridTile cols={5} style={{float: "right"}}>
							<TextField ref="searchBox"
									   hintText="Search All Datasets"
									   onKeyPress={this.handleKeyPressed}
									   value={this.state.searchText}
									   onChange={e => {
										   this.setState({searchText: e.target.value});
									   }}
							/>
							<IconButton iconStyle={{position: "absolute", left: 0, bottom: 5, width: 30, height: 30}}
										onClick={this.searchDatasets}>
								<ActionSearch/>
							</IconButton>
						</GridTile>

					</GridList>
					<GridList cols={12} style={{paddingTop: "10px"}} cellHeight="auto">
						<GridTile cols={5}>
							<h2> Datasets </h2>
							{filtered_datasets}
							<div>
								<GridTile cols={6} style={{paddingTop: "5x", textAlign: "center"}} cellHeight="auto">
									<button disabled={this.state.pageNumber === 1} onClick={this.previous}>
										<FontAwesomeIcon icon={faChevronLeft} transform="grow-4"/> Prev
									</button>
									<button disabled={true}>{this.state.pageNumber}</button>
									<button disabled={this.props.datasets.length < this.state.dataPerPage}
											onClick={this.next}>
										Next <FontAwesomeIcon icon={faChevronRight} transform="grow-4"/></button>
								</GridTile>
							</div>

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
