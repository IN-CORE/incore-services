import React from "react";
import { Table, TableBody, TableRow, TableRowColumn, List, ListItem, Divider } from "material-ui";
import FontAwesomeIcon from "@fortawesome/react-fontawesome";
import {faEye} from "@fortawesome/fontawesome-free-solid/index";

class NestedInfoTable extends React.Component {
	constructor(props) {
		super(props);
	}

	componentDidMount() {
	}

	render() {
		return (<Table bodyStyle={{overflow:"auto"}}>
			<TableBody displayRowCheckbox={false}>
				{
					Object.keys(this.props.data).map((key) => {
						if (key === "hazardDatasets"){
							return (
								<TableRow style={{height:"12px"}}>
									<TableRowColumn style={{height:"12px", width:"30%", fontWeight:"bold", backgroundColor:"#eee"}}>
										{key}
									</TableRowColumn>
									<TableRowColumn style={{height:"12px"}}>
										<List style={{"overflowY": "auto"}}>
											{
												this.props.data[key].map((hazardDataset) => {
													if (hazardDataset.datasetId){
														return (<div key={hazardDataset.datasetId}>
															<ListItem key={hazardDataset.datasetId}
																	  onClick={() => this.props.onClick(hazardDataset.datasetId)}
																	  style={{fontSize:"13px"}}>
																<FontAwesomeIcon icon={faEye}
																				 style={{display: "inline", float:"right", marginRight: "5px"}}/>
																{hazardDataset.datasetId}
															</ListItem>

															{/*details of the dataset*/}
															{(hazardDataset.datasetId === this.props.selectedHazardDataset && this.props.expanded )?
																<NestedInfoTable data={hazardDataset}/> : null }
															<Divider/>
														</div>);
													}
												})
											}
										</List>
									</TableRowColumn>
								</TableRow>
							);
						}
						else if (key === "tornadoDatasetId" && this.props.data[key]){
							return (
								<TableRow style={{height:"12px"}}>
									<TableRowColumn style={{height:"12px", width:"30%", fontWeight:"bold", backgroundColor:"#eee"}}>
										{key}
									</TableRowColumn>
									<TableRowColumn style={{height:"12px"}}>
										<List style={{"overflowY": "auto"}}>
											<ListItem onClick={() => this.props.onClick(this.props.data.tornadoDatasetId)}
													  key={this.props.data.tornadoDatasetId}
													  style={{fontSize:"13px"}}
													  disabled={this.props.data.tornadoDatasetId === this.props.selectedHazardDataset}>
												<FontAwesomeIcon icon={faEye} style={{display: "inline", float:"right", marginRight: "5px"}}/>
												{this.props.data.tornadoDatasetId}
											</ListItem>
											<Divider/>
										</List>
									</TableRowColumn>
								</TableRow>
							);
						}
						else{
							return (
								// first level
								<TableRow style={{height:"12px"}}>
									<TableRowColumn style={{height:"12px", width:"30%", fontWeight:"bold", backgroundColor:"#eee"}}>
										{key}
									</TableRowColumn>

									{	(typeof this.props.data[key] === "object" && this.props.data[key]) ?
										Object.keys(this.props.data[key]).map((key2) =>{
											return (

												// second level
												<TableRow style={{height:"12px"}}>
													<TableRowColumn style={{height:"12px", fontWeight:"bold"}}>
														{key2}
													</TableRowColumn>

													{
														(typeof this.props.data[key][key2] === "object"  && this.props.data[key]) ?
															Object.keys(this.props.data[key][key2]).map((key3) => {
																return (

																	// third level
																	<TableRow style={{height: "12px"}}>
																		<TableRowColumn style={{ height: "12px", fontWeight: "bold"}}>
																			{key3}
																		</TableRowColumn>
																		<TableRowColumn style={{ height: "12px",}}>
																			{JSON.stringify(this.props.data[key][key2][key3])}
																		</TableRowColumn>
																	</TableRow>

																);
															}) : (<TableRowColumn style={{height: "12px"}}>{JSON.stringify(this.props.data[key][key2])}</TableRowColumn>)
													}
												</TableRow>
											);
										})
										:
										(<TableRowColumn style={{height:"12px"}}>{this.props.data[key]}</TableRowColumn>)
									}
								</TableRow> );
						}
					})
				}
			</TableBody>
		</Table>);
	}
}

export default NestedInfoTable;
