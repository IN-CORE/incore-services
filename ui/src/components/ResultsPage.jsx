import React, {Component} from "react";
import Map from "./Map";

class ResultsPage extends Component {

	constructor(props) {
		super(props);
		this.state = {};
	}

	render() {
		let contents;
		contents = <Map/>
		return (
			<div className="main">
				<h2 className="center">{this.props.analysis.name} Results </h2>
			</div>
		);
	}

}

export default ResultsPage;
