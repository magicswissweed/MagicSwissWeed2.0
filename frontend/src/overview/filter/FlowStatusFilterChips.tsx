import "./FlowStatusFilterChips.scss";
import {useState} from "react";
import {Button} from "react-bootstrap";
import {FlowColorEnum} from "../../model/SpotModel";

type FlowFilterChipsProps = {
    initialSelected?: FlowColorEnum[];
    onChange?: (selected: FlowColorEnum[]) => void;
};

export const FlowStatusFilterChips = ({
                                          initialSelected = [FlowColorEnum.GREEN],
                                          onChange,
                                      }: FlowFilterChipsProps) => {
    const [selectedFlows, setSelectedFlows] =
        useState<FlowColorEnum[]>(initialSelected);

    const toggleFlow = (flow: FlowColorEnum) => {
        setSelectedFlows(prev => {
            if (prev.includes(flow)) {
                if (prev.length === 1) return prev; // always at least one
                const next = prev.filter(f => f !== flow);
                onChange?.(next);
                return next;
            }
            const next = [...prev, flow];
            onChange?.(next);
            return next;
        });
    };

    return (
        <div className="flow-chip-container d-flex gap-2">
            <Button
                size="sm"
                variant="light"
                className={`flow-chip green ${selectedFlows.includes(FlowColorEnum.GREEN) ? "active" : ""}`}
                onClick={() => toggleFlow(FlowColorEnum.GREEN)}
            >
                <span className="dot"/>
                Good
            </Button>

            <Button
                size="sm"
                variant="light"
                className={`flow-chip orange ${selectedFlows.includes(FlowColorEnum.ORANGE) ? "active" : ""}`}
                onClick={() => toggleFlow(FlowColorEnum.ORANGE)}
            >
                <span className="dot"/>
                Good forecast
            </Button>

            <Button
                size="sm"
                variant="light"
                className={`flow-chip red ${selectedFlows.includes(FlowColorEnum.RED) ? "active" : ""}`}
                onClick={() => toggleFlow(FlowColorEnum.RED)}
            >
                <span className="dot"/>
                Bad
            </Button>
        </div>
    );
};

