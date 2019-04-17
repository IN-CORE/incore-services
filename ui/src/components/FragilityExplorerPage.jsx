import React from "react";
import GroupList from "../components/GroupList";
import LineChart from "../components/LineChart";
import Notification from "../components/Notification";
import ThreeDimensionalPlot from "../components/ThreeDimensionalPlot";
import "whatwg-fetch";
import {
	SelectField,
	GridList,
	GridTile,
	Card,
	MenuItem,
	TextField,
	Divider,
	IconButton,
	RaisedButton
} from "material-ui";
import ActionSearch from "material-ui/svg-icons/action/search";

// utils
import chartSampler from "../utils/chartSampler";

// config
import chartConfig from "../components/config/ChartConfig";

// application configuration
import config from "../app.config";
import DistributionTable from "./DistributionTable";
import CustomExpressionTable from "./CustomExpressionTable";
import {getHeader} from "../actions";
import {browserHistory} from "react-router";
import FontAwesomeIcon from "@fortawesome/react-fontawesome";
import {faChevronLeft, faChevronRight} from "@fortawesome/fontawesome-free-solid";

class FragilityExplorerPage extends React.Component {

	constructor(props) {
		super(props);

		this.state = {
			selectedInventory: "building",
			selectedHazard: "earthquake",
			selectedDemand: null,
			selectedAuthor: null,
			searchText: "",
			searching: false,
			fragility: null,
			data: [],
			chartConfig: chartConfig.FragilityConfig,
			plotData3d: {},
			authError: false,
			authLocationFrom: sessionStorage.getItem("locationFrom"),
			offset: 0,
			pageNumber: 1,
			dataPerPage: 15
		};

		this.clickFragility = this.clickFragility.bind(this);
		this.handleInventorySelection = this.handleInventorySelection.bind(this);
		this.handleHazardSelection = this.handleHazardSelection.bind(this);
		this.handleSearch = this.handleSearch.bind(this);

		this.handleKeyPressed = this.handleKeyPressed.bind(this);
		this.searchFragilities = this.searchFragilities.bind(this);
		this.queryFragilities = this.queryFragilities.bind(this);

		this.exportJson = this.exportJson.bind(this);
		this.previous = this.previous.bind(this);
		this.next = this.next.bind(this);

		this.changeDataPerPage = this.changeDataPerPage.bind(this);

	}

	async componentWillMount() {
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
		}

		// not logged in
		else {
			this.setState({
				authError: true,
				authLocationFrom: location
			});
		}
	}

	async componentDidMount() {
		let host = config.fragilityService;
		let fragilityId = this.props.params.id;

		let url = `${host}?limit=${this.state.dataPerPage}&skip=${this.state.offset}`;
		if (fragilityId !== undefined) {
			url = `${host}/${fragilityId}`;
		}

		let response = await fetch(url, {method: "GET", mode: "cors", headers: getHeader()});

		if (response.ok) {
			let fragilities = await response.json();

			let fragilitiesWithInfo = [];
			await fragilities.map((fragility) => {
				let is3dPlot = this.is3dFragility(fragility);
				fragility["is3dPlot"] = is3dPlot;
				fragilitiesWithInfo.push(fragility);
			});

			// By default select the first returned in the list of fragilities
			if (fragilitiesWithInfo.length > 0) {
				await this.clickFragility(fragilitiesWithInfo[0]);
			} else {
				await this.clickFragility(fragilitiesWithInfo); // only one fragility (get by id called)
			}


			await this.setState({
				data: fragilitiesWithInfo,
				authError: false
			});
		}
		else if (response.status === 403) {
			// if get 403 forbidden error, means token missing or expired
			this.setState({
				fragility: null,
				data: [],
				authError: true,
			});
		}
		else {
			this.setState({
				fragility: null,
				data: [],
				authError: false
			});
		}
	}

	handleInventorySelection(event, index, value) {
		this.setState({
			searching: false,
			searchText: "",
			selectedInventory: value,
			pageNumber: 1,
			offset: 0
		},
			async function () {
				await this.queryFragilities();
			}
		);
	}

	handleHazardSelection(event, index, value) {
		this.setState({
			searching: false,
			searchText: "",
			selectedHazard: value,
			pageNumber: 1,
			offset: 0
		},
			async function () {
				await this.queryFragilities();
			}
		);
	}

	handleKeyPressed(event) {
		if (event.charCode === 13) { // enter
			event.preventDefault();
			this.handleSearch();
		}
	}

	handleSearch() {
		this.setState({
			searchText: this.refs.searchBox.getValue(),
			searching: true,
			pageNumber: 1,
			offset: 0
		},
			async function () {
				await this.queryFragilities();
			}
		);
	}

	async searchFragilities() {
		let searchText = this.refs.searchBox.getValue();

		let host = config.fragilityService;

		let url = `${host}/search?text=${searchText}&limit=${this.state.dataPerPage}&skip=${this.state.offset}`;

		let response = await fetch(url, {method: "GET", mode: "cors", headers: getHeader()});

		if (response.ok) {
			let fragilities = await response.json();

			if (fragilities.length > 0) {

				let fragilitiesWithInfo = [];

				await fragilities.map((fragility) => {
					let is3dPlot = this.is3dFragility(fragility);
					fragility["is3dPlot"] = is3dPlot;
					fragilitiesWithInfo.push(fragility);
				});

				await this.setState(prevState => ({
					data: fragilitiesWithInfo,
					pageNumber: prevState.searching ? this.state.pageNumber : 1
				}));

				// By default select the first returned in the list of fragilities
				if (fragilitiesWithInfo.length > 0) {
					await this.clickFragility(fragilitiesWithInfo[0]);
				} else {
					await this.clickFragility(fragilitiesWithInfo);
				}
			} else {
				this.setState({
					data: [],
					fragility: null
				});
			}
		} else if (response.status === 403) {
			// if get 403 forbidden error, means token missing or expired
			this.setState({
				fragility: null,
				data: [],
				authError: true,
			});
		}
		else {
			this.setState({
				fragility: null,
				data: []
			});
		}
	}

	async queryFragilities() {
		if (this.state.searchText && this.state.searching) {
			await this.searchFragilities();
			return;
		}

		this.setState({searching: false});

		let host = config.fragilityService;
		let url = "";

		if (this.state.selectedInventory !== null && this.state.selectedHazard !== null) {
			url = `${host}?inventory=${this.state.selectedInventory}&hazard=${this.state.selectedHazard}&limit=${this.state.dataPerPage}&skip=${this.state.offset}`;
		} else if (this.state.selectedInventory !== null) {
			url = `${host}?inventory=${this.state.selectedInventory}&limit=${this.state.dataPerPage}&skip=${this.state.offset}`;
		} else if (this.state.selectedHazard !== null) {
			url = `${host}?hazard=${this.state.selectedHazard}&limit=${this.state.dataPerPage}&skip=${this.state.offset}`;
		} else {
			url = `${host}?limit=${this.state.dataPerPage}&skip=${this.state.offset}`;
		}

		let response = await fetch(url, {method: "GET", mode: "cors", headers: getHeader()});

		if (response.ok) {
			let fragilities = await response.json();

			if (fragilities.length > 0) {

				let fragilitiesWithInfo = [];

				await fragilities.map((fragility) => {
					let is3dPlot = this.is3dFragility(fragility);
					fragility["is3dPlot"] = is3dPlot;
					fragilitiesWithInfo.push(fragility);
				});

				await this.setState({
					data: fragilitiesWithInfo
				});

				// By default select the first returned in the list of fragilities
				if (fragilitiesWithInfo.length > 1) {
					await this.clickFragility(fragilitiesWithInfo[0]);
				} else {
					await this.clickFragility(fragilitiesWithInfo);
				}
			}

			else {
				this.setState({
					data: [],
					fragility: null
				});
			}
		}
		else if (response.status === 403) {
			// if get 403 forbidden error, means token missing or expired
			this.setState({
				fragility: null,
				data: [],
				authError: true,
			});
		}
		else {
			this.setState({
				data: [],
				fragility: null
			});
		}
	}

	previous() {
		this.setState({
			offset: (this.state.pageNumber - 2) * this.state.dataPerPage,
			pageNumber: this.state.pageNumber - 1},
			async function () {
				await this.queryFragilities();
			}
		);
	}

	next() {
		this.setState({
			offset: (this.state.pageNumber) * this.state.dataPerPage,
			pageNumber: this.state.pageNumber + 1},
			async function () {
				await this.queryFragilities();
			}
		);

	}

	changeDataPerPage(event, index, value) {
		this.setState({
			pageNumber: 1,
			offset: 0,
			dataPerPage: value},
			async function () {
				await this.queryFragilities();
			}
		);
	}

	render() {
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
											  onChange={this.changeDataPerPage} style={{maxWidth:"200px"}}>
				<MenuItem primaryText="15" value={15}/>
				<MenuItem primaryText="30" value={30}/>
				<MenuItem primaryText="50" value={50}/>
				<MenuItem primaryText="75" value={75}/>
				<MenuItem primaryText="100" value={100}/>
			</SelectField>);
			return (
				<div style={{padding: "20px", height: "100%"}}>
					<div style={{display: "flex"}}>
						<h2>Fragility Function Viewer</h2>
					</div>

					<GridList cols={12} cellHeight="auto">
						{/* Hazard Type */}
						<GridTile cols={3}>
							<SelectField floatingLabelText="Hazard Type"
										 hintText="Hazard Type" value={this.state.selectedHazard}
										 onChange={this.handleHazardSelection} style={{maxWidth:"200px"}}>
								<MenuItem primaryText="Earthquake" value="earthquake"/>
								<MenuItem primaryText="Tornado" value="tornado"/>
								<MenuItem primaryText="Tsunami" value="tsunami"/>
							</SelectField>
						</GridTile>

						{/* Inventory Type */}
						<GridTile cols={3}>
							<SelectField floatingLabelText="Inventory Type"
										 hintText="Inventory Type" value={this.state.selectedInventory}
										 onChange={this.handleInventorySelection} style={{maxWidth:"200px"}}>
								<MenuItem primaryText="Building" value="building"/>
								<MenuItem primaryText="Bridge" value="bridge"/>
								<Divider/>
								<MenuItem primaryText="Roadway" value="roadway"/>
								<Divider/>
								<MenuItem primaryText="Electric Power Facility" value="electric_facility"/>
								<MenuItem primaryText="Eletric Power Line" value="electric_power_line"/>
								<MenuItem primaryText="Water Facility" value="water_facility"/>
								<MenuItem primaryText="Water Pipeline" value="buried_pipeline"/>
								<MenuItem primaryText="Gas Facility" value="gas_facility"/>
							</SelectField>
						</GridTile>

						{/*Data per page */}
						<GridTile cols={3} style={{float: "left"}}>
							{data_per_page}
						</GridTile>

						{/* Search Box */}
						<GridTile cols={3} style={{float: "right"}}>
							<TextField ref="searchBox" hintText="Search Fragilities"
									   onKeyPress={this.handleKeyPressed}
									   value={this.state.searchText}
									   onChange={e=> {this.setState({searchText:e.target.value});}}/>
							<IconButton iconStyle={{position: "absolute", left: 0, bottom: 5, width: 30, height: 30}}
										onClick={this.handleSearch}>
								<ActionSearch />
							</IconButton>
						</GridTile>

					</GridList>

					<GridList cols={12} style={{paddingTop: "12px"}} cellHeight="auto">
						<GridTile cols={5}>
							<h2>Fragilities</h2>
							<div style={{overflow: "auto", height: "45vh", margin: "0 20px"}}>
								<GroupList id="fragility-list"
										   onClick={this.clickFragility}
										   data={this.state.data} displayField="author"
										   selectedFragility={this.state.fragility}/>
							</div>
							<div>
								<GridTile cols={6} style={{paddingTop: "5x", textAlign:"center"}} cellHeight="auto" >
									<button disabled={this.state.pageNumber === 1} onClick={this.previous}>
										<FontAwesomeIcon icon={faChevronLeft}  transform="grow-4" /> Prev </button>
									<button disabled={true}>{this.state.pageNumber}</button>
									<button disabled={this.state.data.length < this.state.dataPerPage} onClick={this.next}>
										Next <FontAwesomeIcon icon={faChevronRight} transform="grow-4"/> </button>
								</GridTile>
							</div>
						</GridTile>

						{/* Charts */}
						{this.state.fragility ?
							<GridTile cols={7}>
								<h2>Preview</h2>
								<div style={{overflow: "auto", height: "45vh", margin: "0 20px 20px"}}>
									<div style={{marginLeft: "auto", marginBottom: "20px"}}>
										<RaisedButton primary={true} style={{display: "inline-block"}}
													  label="Download Metadata"
													  onClick={this.exportJson}/>
									</div>
									<Card>
										{this.state.fragility.is3dPlot ?
											<div>
												<h3 style={{textAlign: "center"}}>{this.state.plotData3d.title}</h3>
												<ThreeDimensionalPlot plotId="3dplot" data={this.state.plotData3d.data}
																	  xLabel={this.state.fragility.demandType}
																	  yLabel="Y"
																	  zLabel={this.state.fragility.fragilityCurves[0].description}
																	  width="100%" height="350px" style="surface"/>
											</div>
											:
											<LineChart chartId="chart" configuration={this.state.chartConfig}/>}
									</Card>

									{this.state.fragility.fragilityCurves[0].className.includes("CustomExpressionFragilityCurve") ?
										<CustomExpressionTable fragility={this.state.fragility}/>
										:
										<DistributionTable fragility={this.state.fragility}/>}
								</div>
							</GridTile>
							:
							<div></div>
						}

					</GridList>
				</div>
			);
		}
	}

	async clickFragility(fragility) {
		let is3dPlot = this.is3dFragility(fragility);

		let plotData3d = [];
		let plotConfig2d = {};

		if (is3dPlot) {
			plotData3d = await this.generate3dPlotData(fragility);
		} else {
			plotConfig2d = this.generate2dPlotData(fragility);
		}

		this.setState({
			chartConfig: plotConfig2d,
			plotData3d: plotData3d,
			fragility: fragility
		});
	}

	generate2dPlotData(fragility) {
		let updatedChartConfig = Object.assign({}, chartConfig.FragilityConfig);

		let demandType = fragility.demandType !== null ? fragility.demandType : "";
		let demandUnit = fragility.demandUnits !== null ? fragility.demandUnits : "";
		let description = fragility.description !== null ? fragility.description : "";
		let authors = fragility.authors.join(", ");

		updatedChartConfig.xAxis.title.text = `${demandType} (${demandUnit})`;
		updatedChartConfig.title.text = `${description} [${authors}]`;

		updatedChartConfig.series = [];

		for (let i = 0; i < fragility.fragilityCurves.length; i++) {
			let curve = fragility.fragilityCurves[i];

			let plotData;

			if (curve.className.includes("CustomExpressionFragilityCurve")) {
				plotData = chartSampler.computeExpressionSamples(0, 1.0, 90, curve.expression);
			} else if (curve.className.includes("StandardFragilityCurve")) {
				if (curve.curveType === "Normal") { // Actually Log Normal
					plotData = chartSampler.sampleLogNormalCdf(0, 0.999, 1000, curve.median, curve.beta);
				}

				if (curve.curveType === "StandardNormal") {
					plotData = chartSampler.sampleNormalCdf(0, 0.999, 1000, curve.median, curve.beta);
				}

				if (curve.curveType === "LogNormal") { // Log Normal with Normal mean and Normal variance
					plotData = chartSampler.sampleLogNormalAlternate(0, 0.999, 1000, curve.median, curve.beta);
				}
			} else if (curve.className.includes("periodStandardFragilityCurve")) {
				console.log("not implemented");
			} else if (curve.className.includes("buildingPeriodStandardFragilityCurve")) {
				console.log("not implemented");
			} else {
				console.log("not implemented");
			}

			let series = {
				name: curve.description,
				data: plotData
			};

			updatedChartConfig.series.push(series);
		}

		return updatedChartConfig;
	}

	async generate3dPlotData(fragility) {
		let curve = fragility.fragilityCurves[0];
		let plotData = await chartSampler.computeExpressionSamples3d(0.001, 1.0, 50, 0.001, 1.0, 50, curve.expression);

		let description = fragility.description !== null ? fragility.description : "";
		let authors = fragility.authors.join(", ");
		let title = `${description} [${authors}]`;

		return {"data": plotData, "title": title};
	}

	is3dFragility(fragility) {
		let curves = fragility.fragilityCurves;

		for (let i = 0; i < curves.length; i++) {
			let curve = curves[i];

			if (curve.className.includes("CustomExpressionFragilityCurve") && curve.expression.includes("y")) {
				return true;
			}
		}

		return false;
	}

	exportJson() {
		let fragilityJSON = JSON.stringify(this.state.fragility, null, 4);
		let blob = new Blob([fragilityJSON], {type: "application/json"});

		const filename = `${this.state.fragility.id}.json`;

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
}

FragilityExplorerPage.propTypes = {};

export default FragilityExplorerPage;
