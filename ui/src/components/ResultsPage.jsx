import React, {Component} from "react";
import Map from "./Map";
import Table from "./Table";
import {GridList, GridTile} from "material-ui";

class ResultsPage extends Component {

	constructor(props) {
		super(props);
		this.state = {};
	}

	componentWillMount() {
		this.props.getOutputFile(this.props.executionId);
	}

	render() {
		let file_contents;

		if(this.props.fileData) {
			const data = this.props.fileData.map((data) => data.split(","));
			file_contents = <Table height={600} container = "data_container" data={data.slice(2)} colHeaders={data[0]} rowHeaders={false}/>;
		}

		return (
			<div className="main">
				{/*<h2 className="center">{this.props.analysis.name} Results </h2>*/}
				Execution Id: {this.props.executionId}
				<GridList cols={12} cellHeight="auto">
					<GridTile cols={6}>
						{file_contents}
					</GridTile>
					<GridTile cols={6}>
						<Map datasetId={this.props.datasetId}/>
					</GridTile>
				</GridList>

			</div>
		);
	}

}

export default ResultsPage;
