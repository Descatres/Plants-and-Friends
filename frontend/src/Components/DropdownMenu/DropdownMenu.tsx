import * as Dropdown from "@radix-ui/react-dropdown-menu";
import "./DropdownMenu.css";

type DropdownMenuProps = {
  button: any;
  options: { id: number; name: string; onClick: () => void }[];
};

function DropdownMenu({ button, options }: DropdownMenuProps) {
  return (
    <Dropdown.Root>
      <Dropdown.Trigger asChild>{button}</Dropdown.Trigger>
      <Dropdown.Content className="dropdownContent">
        {options?.map((option) => (
          <Dropdown.Item
            key={option.id}
            onClick={option.onClick}
            className="dropdownItem"
          >
            {option.name}
          </Dropdown.Item>
        ))}
      </Dropdown.Content>
    </Dropdown.Root>
  );
}

export default DropdownMenu;
