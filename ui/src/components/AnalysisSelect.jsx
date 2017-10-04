import React, {Component} from "react";
import Select from "./material/Select";

class AnalysisSelect extends Component {

	constructor(props) {
		super(props);
		this.state = {}
		this.onChange = this.onChange.bind(this);
	}
	onChange() {

	}

	render() {
		const options = this.props.analyses.map( d =>
			 <option value={d.id} key={d.id} > {d.name}</option>
		);
		
		return (
			<div>

				<Select onChange={this.onChange}>
					{options}
				</Select>
			</div>
		)
	}

}

export default AnalysisSelect;
