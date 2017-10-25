import React, { PropTypes } from "react";
import HighCharts from "highcharts";

class LineChart extends React.Component {
	constructor(props) {
		super(props);
	}

	componentDidMount() {
		HighCharts.chart(this.props.chartId, this.props.configuration);
	}

	componentDidUpdate() {
		HighCharts.chart(this.props.chartId, this.props.configuration);
	}

	render() {
		return (<div id={this.props.chartId} className="highcharts-container" />);
	}
}

LineChart.propTypes = {};

export default LineChart;
