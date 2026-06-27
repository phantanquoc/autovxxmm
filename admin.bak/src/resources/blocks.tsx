import {
  Create,
  Datagrid,
  DateField,
  List,
  NumberField,
  ReferenceField,
  ReferenceInput,
  required,
  SelectInput,
  SimpleForm,
  TextField,
  TextInput,
} from 'react-admin';

export const BlockList = () => (
  <List sort={{ field: 'id', order: 'DESC' }} perPage={50}>
    <Datagrid rowClick={false} bulkActionButtons={false}>
      <NumberField source="id" />
      <ReferenceField source="serverId" reference="servers" link={false}>
        <TextField source="name" />
      </ReferenceField>
      <TextField source="name" label="Người chơi" />
      <TextField source="reason" />
      <DateField source="createdAt" showTime />
    </Datagrid>
  </List>
);

export const BlockCreate = () => (
  <Create>
    <SimpleForm>
      <ReferenceInput source="serverId" reference="servers">
        <SelectInput optionText="name" validate={required()} />
      </ReferenceInput>
      <TextInput source="name" label="Tên người chơi" validate={required()} />
      <TextInput source="reason" label="Lý do" />
    </SimpleForm>
  </Create>
);
