import React, {Component} from "react";
import Table from "./Table";
import Map from "./Map";
import {GridList, GridTile, SelectField, MenuItem, List,
	ListItem, Divider, TextField, IconButton} from "material-ui";
import ActionSearch from "material-ui/svg-icons/action/search";
import csv from "csv";
import config from "../app.config";
import {getHeader} from "../actions";

String.prototype.capitalize = function() {
	return this.charAt(0).toUpperCase() + this.slice(1);
};

class DataViewer extends Component {

	constructor(props) {
		super(props);
		this.state = {
			type: "",
			selectedDataset: "",
			selectedDatasetFormat: "",
			fileData: "",
			fileExtension: "",
			space: ""
		};
		this.changeDatasetType = this.changeDatasetType.bind(this);
		this.onClickDataset = this.onClickDataset.bind(this);
		this.handleKeyPressed = this.handleKeyPressed.bind(this);
		this.searchDatasets = this.searchDatasets.bind(this);
		this.onClickFileDescriptor = this.onClickFileDescriptor.bind(this);
		this.changeSpace = this.changeSpace.bind(this);
	}

	componentWillMount() {
		this.props.getAllDatasets();
	}

	changeDatasetType(event, index, value) {
		this.setState({type: value, space: "", selectedDataset: "", fileData: "", fileExtension: "", selectedDatasetFormat: ""});
	}

	changeSpace(event, index, value) {
		this.setState({space: value, type:"", selectedDataset: "", fileData: "", fileExtension: "", selectedDatasetFormat: ""});
	}

	onClickDataset(datasetId) {
		const dataset = this.props.datasets.find(dataset => dataset.id === datasetId);
		this.setState({selectedDataset: datasetId, selectedDatasetFormat: dataset.format, fileData: "", fileExtension: ""});

	}

	async handleKeyPressed(event) {
		if (event.charCode === 13) { // enter
			event.preventDefault();
			await this.searchDatasets();
		}
	}

	async searchDatasets() {
		let searchText = this.refs.searchBox.getValue();

		this.setState({searchText: searchText, selectedDataset: "", fileData: "", fileExtension: ""});
	}

	async onClickFileDescriptor(selected_dataset_id, file_descriptor_id, file_name) {
		const url = `${config.dataService}/files/${  file_descriptor_id  }/file`;
		await fetch(url, {method: "GET", mode: "CORS", headers: getHeader()}).then((response) => {
			return response.text();
		}).then((text) => {
			this.setState({fileData: text.split("\n"), fileExtension: file_name.split(".").slice(-1).pop()});
		});

	}


	render() {
		let dataset_types = "";
		const dataset_all_types = this.props.datasets.map(dataset =>
			dataset.type
		);
		const unique_types = Array.from(new Set(dataset_all_types));
		if(unique_types.length > 0) {
			const type_menu_items = unique_types.map((type, index) =>
				<MenuItem value={index} primaryText={type} key={type}/>
			);
			dataset_types = (<SelectField fullWidth={true}
			floatingLabelText="Dataset Type"
			value={this.state.type}
			onChange={this.changeDatasetType}
			>
				{type_menu_items}
				</SelectField>);
		}

		let space_filter = "";
		const spaces = this.props.datasets.map(dataset => dataset.spaces);
		const unique_spaces = Array.from(new Set( [].concat.apply([], spaces)));

		if(unique_spaces.length > 0){
			const space_menu_items = unique_spaces.map((space, index) =>
				<MenuItem value={index} primaryText={space} key={space}/>
			);

			space_filter = (<SelectField fullWidth={true}
			                               floatingLabelText="Space"
			                               value={this.state.space}
			                               onChange={this.changeSpace}
			>
				{space_menu_items}
				</SelectField>
				);
		}

		let datasets_to_display = this.props.datasets;
		if(this.state.type) {
			datasets_to_display = this.props.datasets.filter(dataset => dataset.type === unique_types[this.state.type]);
		}
		if(this.state.space){
			datasets_to_display = datasets_to_display.filter(dataset => dataset.spaces.indexOf(unique_spaces[this.state.space]) > -1);
		}
		if(this.state.searchText){
			datasets_to_display = datasets_to_display.filter(dataset => dataset.title.indexOf(this.state.searchText) > -1);
		}



		const list_items = datasets_to_display.map(dataset =>
			<div key={dataset.id}>
				<ListItem onClick={() => this.onClickDataset(dataset.id)} key={dataset.id} primaryText={`${dataset.title  } - ${  dataset.creator.capitalize()}`}/>
				<Divider/>
			</div>
		);

		const filtered_datasets = (<div>
			<h2> Datasets </h2><List style={{"overflowY": "auto", height: "800px"}}>
			{list_items}
		</List>
		</div>);

		let file_descriptors ="";
		if(this.state.selectedDataset){
			const selected_dataset = this.props.datasets.find(dataset => dataset.id === this.state.selectedDataset);
			file_descriptors =
				selected_dataset.fileDescriptors.map(file_descriptor =>
					<div key={file_descriptor.id}>
						<ListItem onClick={() => this.onClickFileDescriptor(selected_dataset.id, file_descriptor.id, file_descriptor.filename)} primaryText={file_descriptor.filename} key={file_descriptor.id}/>
						<Divider/>
					</div>

			);
		}
		let file_contents = this.state.fileData;
		if(this.state.fileExtension && this.state.fileData  && this.state.fileExtension === "csv") {
			let data = this.state.fileData.map((data) => data.split(","));
			file_contents = <Table height={750} container = "data_container" data={data.slice(2)} colHeaders={data[0]} rowHeaders={false}/>;
		}

		let right_column =  "";
		if(this.state.selectedDatasetFormat === "shapefile") {
			right_column =
				(<div>
					<Map datasetId={this.state.selectedDataset}/>
				</div>);
		} else if (file_descriptors.length > 0) {
			right_column = (<div>
				<h2> File Descriptors</h2>
				<List style={{"overflowY": "auto", height: "50px"}}>
					{file_descriptors}
				</List>
			</div>);
		}

		return (
			<div style={{padding: "20px"}}>
				<div style={{display:"flex"}}>
					<h2>Data Viewer</h2>
				</div>
				<GridList cols={12} cellHeight="auto">
					<GridTile cols={4}>
						{dataset_types}
					</GridTile>
					<GridTile cols={4}>
						{space_filter}
					</GridTile>
					<GridTile cols={4} style={{float: "right"}}>
						<TextField ref="searchBox" hintText="Search Datasets" onKeyPress={this.handleKeyPressed} />
						<IconButton iconStyle={{position: "absolute", left: 0, bottom: 5, width: 30, height: 30}}
						            onClick={this.searchDatasets}>
							<ActionSearch />
						</IconButton>
					</GridTile>
				</GridList>
				<GridList cols={12} style={{paddingTop: "10px"}} cellHeight="auto">
					<GridTile cols={6}>
						{filtered_datasets}
					</GridTile>
					<GridTile cols={6}>
						{right_column}
						{file_contents}
					</GridTile>
				</GridList>
			</div>
		);
	}

}

export default DataViewer;
