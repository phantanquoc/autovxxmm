import {
  Datagrid,
  DateField,
  List,
  NumberField,
  ReferenceField,
  ReferenceInput,
  SelectField,
  SelectInput,
  TextField,
  TextInput,
} from 'react-admin';

const STATUS_CHOICES = [
  { id: 0, name: 'WAIT' },
  { id: 1, name: 'BET' },
  { id: 2, name: 'LOSE' },
  { id: 3, name: 'WIN' },
  { id: 4, name: 'REWARD' },
  { id: 5, name: 'ERROR' },
];

const filters = [
  <TextInput key="q" source="q" label="Tìm (khách/bot)" alwaysOn />,
  <ReferenceInput key="srv" source="serverId" reference="servers">
    <SelectInput optionText="name" label="Server" />
  </ReferenceInput>,
  <SelectInput key="st" source="status" choices={STATUS_CHOICES} label="Trạng thái" />,
];

export const OrderList = () => (
  <List filters={filters} sort={{ field: 'id', order: 'DESC' }} perPage={25}>
    <Datagrid rowClick={false} bulkActionButtons={false}>
      <NumberField source="id" />
      <ReferenceField source="serverId" reference="servers" link={false}>
        <TextField source="name" />
      </ReferenceField>
      <TextField source="name" label="Khách" />
      <TextField source="bot" label="Bot" />
      <NumberField source="second" label="Giây" />
      <NumberField source="coinOrder" label="Xu cược" />
      <NumberField source="coinWin" label="Xu thắng" />
      <NumberField source="coinReward" label="Xu thưởng" />
      <SelectField source="status" choices={STATUS_CHOICES} />
      <DateField source="createdAt" showTime />
    </Datagrid>
  </List>
);
