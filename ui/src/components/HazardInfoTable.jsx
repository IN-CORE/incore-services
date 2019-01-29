import React from "react";
import { Table, TableBody, TableRow, TableRowColumn } from "material-ui";

class HazardInfoTable extends React.Component {
	constructor(props) {
		super(props);
	}

	componentDidMount() {
	}

	render() {
		return (<Table bodyStyle={{overflow:"visible"}}>
			<TableBody displayRowCheckbox={false}>
				{
					Object.keys(this.props.selected_hazard_detail).map((key) => {
						return (
							// first level
							<TableRow key={`${key}-"row"`} style={{height:"12px", overflow: "auto"}}>
								<TableRowColumn key={`${key}-"rowColumn"`}
												style={{height:"12px", fontSize:"12px", width: "30%", fontWeight:"bold", backgroundColor:"#eee"}}>
									{key}
								</TableRowColumn>

								{	(typeof this.props.selected_hazard_detail[key] === "object" && this.props.selected_hazard_detail[key]) ?
									Object.keys(this.props.selected_hazard_detail[key]).map((key2) =>{
										return (

											// second level
											<TableRow key={`${key2}-"row"`} style={{height:"12px"}}>
												<TableRowColumn key={`${key2}-"rowColumn"`} style={{height:"12px", fontSize:"12px", width: "30%", fontWeight:"bold"}}>
													{key2}
												</TableRowColumn>

												{
													(typeof this.props.selected_hazard_detail[key][key2] === "object"  && this.props.selected_hazard_detail[key]) ?
														Object.keys(this.props.selected_hazard_detail[key][key2]).map((key3) => {
															return (

																// third level
																<TableRow key={`${key3}-"row"`} style={{height: "12px"}}>
																	<TableRowColumn key={`${key3}-"rowColumn"`} style={{
																		height: "12px",
																		width: "30%",
																		fontWeight: "bold",
																		fontSize:"12px",
																	}}>{key3}</TableRowColumn>
																	<TableRowColumn key={`${key3}-"valueColumn"`} style={{
																		height: "12px",
																		width: "30%",
																		fontSize:"12px",
																	}}>{JSON.stringify(this.props.selected_hazard_detail[key][key2][key3])}</TableRowColumn>
																</TableRow>

															);
														}) : (<TableRowColumn key={`${key2}-"valueColumn"`}
															style={{height: "12px"}}>{JSON.stringify(this.props.selected_hazard_detail[key][key2])}</TableRowColumn>)
												}
											</TableRow>
										);
									}):(<TableRowColumn key={`${key}-"valueColumn"`} style={{height:"12px"}}>{this.props.selected_hazard_detail[key]}</TableRowColumn>)
								}
							</TableRow> );
					})
				}
			</TableBody>
		</Table>);
	}
}

export default HazardInfoTable;
