import React from "react";
import GroupList from "../components/GroupList";
import LineChart from "../components/LineChart";
import ThreeDimensionalPlot from "../components/ThreeDimensionalPlot";
import "whatwg-fetch";

import { SelectField, GridList, GridTile, Card, MenuItem } from "material-ui";
import ArrowDropRight from "material-ui/svg-icons/navigation-arrow-drop-right";

// utils
import chartSampler from "../utils/chartSampler";

// config
import chartConfig from "../components/config/ChartConfig";

// application configuration
import config from "../app.config";

class FragilityExplorerPage extends React.Component {
	constructor(props) {
		super(props);

		this.state = {
			data: [],
			chartConfig: chartConfig.FragilityConfig,
			plotData3d: [],
			is3dPlot: false
		};

		this.clickFragility = this.clickFragility.bind(this);
	}

	async componentDidMount() {
		let host = config.fragilityService;
		let fragilityId = this.props.params.id;

		let url = host;
		if (fragilityId !== undefined) {
			url = `${host}/${fragilityId}`;
		}

		let response = await fetch(url, {method: "GET", mode: "cors"});

		if (response.ok) {
			let fragilities = await response.json();

			// By default select the first returned in the list of fragilities
			await this.clickFragility(fragilities[0]);

			this.setState({
				data: fragilities
			});
		}
	}

	render() {
		return (
			<div>
				<h2 style={{"paddingLeft": "20px"}}>Fragility Function Viewer</h2>
				<GridList cols={12} cellHeight="auto">
					{/* Inventory Type */}
					<GridTile cols={2}>
						<SelectField floatingLabelText="Inventory Type">
							<MenuItem primaryText="Building" />
							<MenuItem primaryText="Bridge" />

							<MenuItem primaryText="Transportation" rightIcon={<ArrowDropRight />}
									  menuItems={[
										  <MenuItem primaryText="Roadway" />,
										  <MenuItem primaryText="Railway" />
									  ]}
							/>

							<MenuItem primaryText="Lifeline" rightIcon={<ArrowDropRight />}
									  menuItems={[
										  <MenuItem primaryText="Electric Power Network" />,
										  <MenuItem primaryText="Potable Water Network" />
									  ]} />
						</SelectField>
					</GridTile>

					{/* Hazard Type */}
					<GridTile cols={2}>
						<SelectField floatingLabelText="Hazard Type">
							<MenuItem primaryText="Earthquake" />
							<MenuItem primaryText="Tornado" />
							<MenuItem primaryText="Tsunami" />
						</SelectField>
					</GridTile>

					{/* Authors */}
					<GridTile cols={2}>
						<SelectField floatingLabelText="Author">
							<MenuItem primaryText="John M. Eidinger" />
							<MenuItem primaryText="Test" />
						</SelectField>
					</GridTile>
				</GridList>

				<GridList cols={12} style={{"paddingTop": "10px"}} cellHeight="auto">
					<GridTile cols={6}>
						<GroupList id="fragility-list" onClick={this.clickFragility} height="500px"
								   data={this.state.data} displayField="author" />
					</GridTile>
					<GridTile cols={6}>
						<Card>
							{this.state.is3dPlot ?
								<ThreeDimensionalPlot plotId="3dplot" data={this.state.plotData3d}
													  xLabel={this.state.fragility.demandType} yLabel="Y"
													  zLabel={this.state.fragility.fragilityCurves[0].description}
													  width="100%" height="500px" style="surface" />
								:
								<LineChart chartId="chart" configuration={this.state.chartConfig} />}
						</Card>
					</GridTile>
				</GridList>
			</div>
		);
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

		this.setState(
			{
				chartConfig: plotConfig2d,
				plotData3d: plotData3d,
				is3dPlot: is3dPlot,
				fragility: fragility
			});
	}

	generate2dPlotData(fragility) {
		let updatedChartConfig = Object.assign({}, chartConfig.FragilityConfig);

		updatedChartConfig.xAxis.title.text = `${fragility.demandType} (${fragility.demandUnits})`;
		updatedChartConfig.title.text = `${fragility.description} (${fragility.authors.join(", ")})`;

		updatedChartConfig.series = [];

		for (let i = 0; i < fragility.fragilityCurves.length; i++) {
			let curve = fragility.fragilityCurves[i];

			let plotData;

			if (curve.className.includes("CustomExpressionFragilityCurve")) {
				plotData = chartSampler.computeExpressionSamples(0, 1.0, 100, curve.expression);
			} else if (curve.className.includes("StandardFragilityCurve")) {
				if (curve.curveType === "Normal") {
					plotData = chartSampler.computeLogNormalCdfSamplesXAxis(0, 0.999, 1000, curve.median, curve.beta);
				}

				if (curve.curveType === "LogNormal") {
					plotData = chartSampler.computeLogNormalCdfSamplesXAxis(0, 0.999, 1000, curve.median, curve.beta);
				}
			} else if (curve.className.includes("periodStandardFragilityCurve")) {

			} else if (curve.className.includes("buildingPeriodStandardFragilityCurve")) {

			} else {

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

		return plotData;
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
	}

FragilityExplorerPage.propTypes = {};

export default FragilityExplorerPage;
