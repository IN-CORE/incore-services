let FragilityConfig = {
	credits: false,
	title: {
		text: "Fragility Viewer",
		x: -20 //center
	},
	xAxis: {
		title: {
			text: "PGA (g)"
		}
	},
	yAxis: {
		title: {
			text: "Probability of Exceedance"
		},
		min: 0.0,
		max: 1.0,
		plotLines: [{
			value: 0,
			width: 1,
			color: "#808080"
		}]
	},
	tooltip: {
		valueSuffix: ""
	},
	legend: {
		layout: "vertical",
		align: "right",
		verticalAlign: "middle",
		borderWidth: 0
	},
	series: []
};

export default {FragilityConfig};
