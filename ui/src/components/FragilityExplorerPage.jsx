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

class FragilityExplorerPage extends React.Component {
	constructor(props) {
		super(props);

		this.state = {
			selectedInventory: "building",
			selectedHazard: "earthquake",
			selectedDemand: null,
			selectedAuthor: null,
			searchText: "",
			fragility: null,
			data: [],
			chartConfig: chartConfig.FragilityConfig,
			plotData3d: {},
			authError: false,
			authLocationFrom: sessionStorage.getItem("locationFrom")
		};

		this.clickFragility = this.clickFragility.bind(this);
		this.handleAuthorSelection = this.handleAuthorSelection.bind(this);
		this.handleInventorySelection = this.handleInventorySelection.bind(this);
		this.handleHazardSelection = this.handleHazardSelection.bind(this);
		this.handleDemandSelection = this.handleDemandSelection.bind(this);

		this.handleKeyPressed = this.handleKeyPressed.bind(this);
		this.searchFragilities = this.searchFragilities.bind(this);

		this.exportJson = this.exportJson.bind(this);
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

		let url = host;
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

	async handleInventorySelection(event, index, value) {
		await this.setState({selectedInventory: value});
		await this.queryFragilities();
	}

	async handleHazardSelection(event, index, value) {
		await this.setState({selectedHazard: value});
		await this.queryFragilities();
	}

	async handleDemandSelection(event, index, value) {
		await this.setState({selectedDemand: value});
		await this.queryFragilities();
	}

	async handleAuthorSelection(event, index, value) {
		await this.setState({selectedAuthor: value});
		await this.queryFragilities();
	}

	async handleKeyPressed(event) {
		if (event.charCode === 13) { // enter
			event.preventDefault();
			await this.searchFragilities();
		}
	}

	async searchFragilities() {
		let searchText = this.refs.searchBox.getValue();

		let host = config.fragilityService;

		let url = `${host}/search?text=${searchText}`;

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
		let host = config.fragilityService;

		let url = "";

		if (this.state.selectedInventory !== null && this.state.selectedHazard !== null) {
			url = `${host}?inventory=${this.state.selectedInventory}&hazard=${this.state.selectedHazard}`;
		} else if (this.state.selectedInventory !== null) {
			url = `${host}?inventory=${this.state.selectedInventory}`;
		} else if (this.state.selectedHazard !== null) {
			url = `${host}?hazard=${this.state.selectedHazard}`;
		} else {
			url = `${host}`;
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
			return (
				<div style={{padding: "20px"}}>
					<div style={{display: "flex"}}>
						<h2>Fragility Function Viewer</h2>
					</div>

					<GridList cols={12} cellHeight="auto">
						{/* Hazard Type */}
						<GridTile cols={3}>
							<SelectField fullWidth={true} floatingLabelText="Hazard Type"
										 hintText="Hazard Type" value={this.state.selectedHazard}
										 onChange={this.handleHazardSelection}>
								<MenuItem primaryText="Earthquake" value="earthquake"/>
								<MenuItem primaryText="Tornado" value="tornado"/>
								<MenuItem primaryText="Tsunami" value="tsunami"/>
							</SelectField>
						</GridTile>

						{/* Inventory Type */}
						<GridTile cols={3}>
							<SelectField fullWidth={true} floatingLabelText="Inventory Type"
										 hintText="Inventory Type" value={this.state.selectedInventory}
										 onChange={this.handleInventorySelection}>
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

						{/* Search Box */}
						<GridTile cols={6} style={{float: "right"}}>
							<TextField ref="searchBox" hintText="Search Fragilities" onKeyPress={this.handleKeyPressed}/>
							<IconButton iconStyle={{position: "absolute", left: 0, bottom: 5, width: 30, height: 30}}
										onClick={this.searchFragilities}>
								<ActionSearch/>
							</IconButton>
						</GridTile>
					</GridList>

					<GridList cols={12} style={{paddingTop: "12px"}} cellHeight="auto">
						<GridTile cols={5}>
							<h2>Fragilities</h2>
							<div style={{overflow: "auto", height: "600px", margin: "0 20px"}}>
								<GroupList id="fragility-list"
										   onClick={this.clickFragility}
										   data={this.state.data} displayField="author"
										   selectedFragility={this.state.fragility}/>
							</div>
						</GridTile>

						{/* Charts */}
						{this.state.fragility ?
							<GridTile cols={7}>
								<h2>Preview</h2>
								<div style={{overflow: "auto", height: "600px", margin: "0 20px"}}>
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
