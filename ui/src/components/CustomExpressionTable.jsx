import React, { PropTypes } from "react";
import { Table, TableBody, TableHeader, TableHeaderColumn, TableRow, TableRowColumn } from "material-ui";

class CustomExpressionTable extends React.Component {
	constructor(props) {
		super(props);
	}

	componentDidMount() {
	}

	render() {
		return (
			<Table>
				<TableHeader displaySelectAll={false} adjustForCheckbox={false}>
					<TableRow>
						<TableHeaderColumn colSpan="2" tooltip="Fragility GUID"
										   style={{textAlign: "center"}}>
							{this.props.fragility.id}
						</TableHeaderColumn>
					</TableRow>
					<TableRow>
						<TableHeaderColumn>Limit State</TableHeaderColumn>
						<TableHeaderColumn>Expression</TableHeaderColumn>
					</TableRow>
				</TableHeader>
				<TableBody displayRowCheckbox={false}>
					{this.props.fragility.fragilityCurves.map(function (curve) {
						return (
							<TableRow>
								<TableRowColumn>{curve.description}</TableRowColumn>
								<TableRowColumn>{curve.expression}</TableRowColumn>
							</TableRow>
						);
					})}
				</TableBody>
			</Table>
		);
	}

	componentWillUnmount() {
	}
}

CustomExpressionTable.propTypes = {};

export default CustomExpressionTable;
