import { useMutation, useQuery, useQueryClient, type QueryClient } from '@tanstack/react-query';
import { api } from '../../lib/api';
import type { Address, PostalCode, Role, UserDetails, UserStatus, UserSummary } from '../../types';

interface ProfilePhotoResponse {
  version: number;
}

export interface CreateUserInput {
  name: string;
  cpf: string;
  birthDate: string;
  password: string;
  role: Role;
}

export interface AddressInput {
  zipCode: string;
  number: string;
  complement?: string;
  primary: boolean;
}

export interface UpdateUserInput {
  name: string;
  cpf: string;
  birthDate: string;
  role?: Role;
}

export function useUsers(enabled = true) {
  return useQuery({
    queryKey: ['users'],
    queryFn: async () => (await api.get<UserSummary[]>('/users')).data,
    enabled,
  });
}

export function useUser(id?: string) {
  return useQuery({
    queryKey: ['users', id],
    queryFn: async () => (await api.get<UserDetails>(`/users/${id}`)).data,
    enabled: Boolean(id),
  });
}

export function useCreateUser() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: async (input: CreateUserInput) =>
      (await api.post<UserSummary>('/users', input)).data,
    onSuccess: () => client.invalidateQueries({ queryKey: ['users'] }),
  });
}

export function useUpdateUser(userId: string) {
  const client = useQueryClient();
  return useMutation({
    mutationFn: async (input: UpdateUserInput) =>
      (await api.put<UserDetails>(`/users/${userId}`, input)).data,
    onSuccess: (updated) => {
      client.setQueryData(['users', userId], updated);
      client.invalidateQueries({ queryKey: ['users'] });
    },
  });
}

export function useUpdateUserStatus(userId: string) {
  const client = useQueryClient();
  return useMutation({
    mutationFn: async (status: UserStatus) =>
      (await api.patch<UserDetails>(`/users/${userId}/status`, { status })).data,
    onSuccess: (updated) => {
      client.setQueryData(['users', userId], updated);
      client.setQueryData<UserSummary[]>(['users'], (current) =>
        current?.map((user) => (user.id === userId ? { ...user, status: updated.status } : user)),
      );
    },
  });
}

export function useProfilePhoto(userId: string, version?: number) {
  return useQuery({
    queryKey: ['profile-photo', userId, version],
    queryFn: async () =>
      (await api.get<Blob>(`/users/${userId}/photo`, { responseType: 'blob' })).data,
    enabled: version !== undefined,
    staleTime: Number.POSITIVE_INFINITY,
  });
}

function updatePhotoVersion(client: QueryClient, userId: string, version?: number) {
  client.setQueryData<UserDetails>(['users', userId], (current) =>
    current ? { ...current, profilePhotoVersion: version } : current,
  );
  client.setQueryData<UserSummary[]>(['users'], (current) =>
    current?.map((user) => (user.id === userId ? { ...user, profilePhotoVersion: version } : user)),
  );
}

export function useUploadProfilePhoto(userId: string) {
  const client = useQueryClient();
  return useMutation({
    mutationFn: async (file: File) => {
      const body = new FormData();
      body.append('file', file);
      return (await api.put<ProfilePhotoResponse>(`/users/${userId}/photo`, body)).data;
    },
    onSuccess: ({ version }) => {
      client.removeQueries({ queryKey: ['profile-photo', userId] });
      updatePhotoVersion(client, userId, version);
    },
  });
}

export function useDeleteProfilePhoto(userId: string) {
  const client = useQueryClient();
  return useMutation({
    mutationFn: async () => api.delete(`/users/${userId}/photo`),
    onSuccess: () => {
      client.removeQueries({ queryKey: ['profile-photo', userId] });
      updatePhotoVersion(client, userId);
    },
  });
}

export function useCreateAddress(userId: string) {
  const client = useQueryClient();
  return useMutation({
    mutationFn: async (input: AddressInput) =>
      (await api.post<Address>(`/users/${userId}/addresses`, input)).data,
    onSuccess: () => client.invalidateQueries({ queryKey: ['users', userId] }),
  });
}

export function useUpdateAddress(userId: string, addressId: string) {
  const client = useQueryClient();
  return useMutation({
    mutationFn: async (input: AddressInput) =>
      (await api.put<Address>(`/users/${userId}/addresses/${addressId}`, input)).data,
    onSuccess: () => client.invalidateQueries({ queryKey: ['users', userId] }),
  });
}

export function useSetPrimary(userId: string) {
  const client = useQueryClient();
  return useMutation({
    mutationFn: async (addressId: string) =>
      (await api.patch<Address>(`/users/${userId}/addresses/${addressId}/primary`)).data,
    onSuccess: () => client.invalidateQueries({ queryKey: ['users', userId] }),
  });
}

export function useDeleteAddress(userId: string) {
  const client = useQueryClient();
  return useMutation({
    mutationFn: async (addressId: string) => api.delete(`/users/${userId}/addresses/${addressId}`),
    onSuccess: () => client.invalidateQueries({ queryKey: ['users', userId] }),
  });
}

export async function lookupPostalCode(zipCode: string) {
  return (await api.get<PostalCode>(`/postal-codes/${zipCode.replace(/\D/g, '')}`)).data;
}
