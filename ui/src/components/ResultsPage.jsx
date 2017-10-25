import React, {Component} from "react";
import {Display2} from "react-mdc-web";
import Map from "./Map";

class ResultsPage extends Component {

	constructor(props) {
		super(props);
		this.state = {};
	}

	render() {
		let contents;
		contents = <Map/>;
		return (
			<div className="main">
				<Display2 className="center"> {this.props.analysis.name} Results </Display2>
				{contents}
			</div>
		);
	}

}

export default ResultsPage;
