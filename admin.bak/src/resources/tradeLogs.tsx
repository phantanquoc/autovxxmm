import { Datagrid, DateField, List, NumberField, ReferenceInput, SelectInput, TextField, TextInput } from 'react-admin';

const filters = [
  <TextInput key="q" source="q" label="Tìm kiếm" alwaysOn />,
  <ReferenceInput key="srv" source="serverId" reference="servers">
    <SelectInput optionText="name" label="Server" />
  </ReferenceInput>,
];

export const TradeLogList = () => (
  <List filters={filters} sort={{ field: 'id', order: 'DESC' }} perPage={25}>
    <Datagrid rowClick={false} bulkActionButtons={false}>
      <NumberField source="id" />
      <NumberField source="serverId" label="SV" />
      <TextField source="name" label="Bot" />
      <TextField source="customer" label="Khách" />
      <NumberField source="before" />
      <NumberField source="after" />
      <NumberField source="change" label="Thay đổi" />
      <NumberField source="type" label="Loại" />
      <DateField source="createdAt" showTime />
    </Datagrid>
  </List>
);
