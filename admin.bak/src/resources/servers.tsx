import {
  Create,
  Datagrid,
  DateField,
  Edit,
  List,
  NumberField,
  NumberInput,
  required,
  SimpleForm,
  TextField,
  TextInput,
} from 'react-admin';

const filters = [<TextInput key="q" source="q" label="Tìm" alwaysOn />];

export const ServerList = () => (
  <List filters={filters} perPage={50} sort={{ field: 'id', order: 'ASC' }}>
    <Datagrid rowClick="edit" bulkActionButtons={false}>
      <NumberField source="id" />
      <TextField source="name" />
      <TextField source="ip" />
      <NumberField source="port" />
      <NumberField source="type" />
    </Datagrid>
  </List>
);

const ServerForm = (creating: boolean) => (
  <SimpleForm>
    <NumberInput source="id" validate={required()} disabled={!creating} />
    <TextInput source="name" validate={required()} />
    <TextInput source="ip" validate={required()} />
    <NumberInput source="port" validate={required()} defaultValue={14444} />
    <NumberInput source="type" defaultValue={0} />
  </SimpleForm>
);

export const ServerCreate = () => <Create>{ServerForm(true)}</Create>;
export const ServerEdit = () => <Edit mutationMode="pessimistic">{ServerForm(false)}</Edit>;
