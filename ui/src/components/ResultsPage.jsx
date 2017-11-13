import React, {Component} from "react";
import Map from "./Map";

class ResultsPage extends Component {

	constructor(props) {
		super(props);
		this.state = {};
	}

	render() {
		return (
			<div className="main">
				<h2 className="center">{this.props.analysis.name} Results </h2>
				Execution Id: {this.props.executionId}
				<Map/>
			</div>
		);
	}

}

export default ResultsPage;
