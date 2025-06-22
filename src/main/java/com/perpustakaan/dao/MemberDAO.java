package com.perpustakaan.dao;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.perpustakaan.model.Member;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemberDAO {
    // File path untuk menyimpan data anggota
    private static final String CSV_FILE_PATH = "data/members.csv";

    /**
     * Membaca semua anggota dari file CSV.
     * @return List berisi semua anggota.
     */
    public List<Member> getAllMembers() {
        List<Member> members = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(CSV_FILE_PATH))) {
            reader.skip(1);
            List<String[]> records = reader.readAll();
            for (String[] record : records) {
                members.add(new Member(record[0], record[1], record[2], record[3], record[4], record[5], record[6], record[7]));
            }
        } catch (IOException | CsvException e) { e.printStackTrace(); }
        return members;
    }

    /**
     * Mencari anggota berdasarkan ID anggota.
     * @param memberId ID anggota yang dicari.
     * @return Optional berisi anggota jika ditemukan, atau kosong jika tidak ditemukan.
     */
    public Optional<Member> getMemberById(String memberId) {
        return getAllMembers().stream().filter(member -> member.getMemberId().equals(memberId)).findFirst();
    }

    /**
     * Menambahkan anggota baru ke dalam file CSV.
     * @param newMember Anggota baru yang akan ditambahkan.
     */
    public void addMember(Member newMember) {
        List<Member> members = getAllMembers();
        members.add(newMember);
        writeAllMembers(members);
    }
    
    /**
     * Memperbarui data anggota yang sudah ada.
     * @param updatedMember Anggota dengan data yang diperbarui.
     */
    public void updateMember(Member updatedMember) {
        List<Member> members = getAllMembers();
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getMemberId().equals(updatedMember.getMemberId())) {
                members.set(i, updatedMember);
                break;
            }
        }
        writeAllMembers(members);
    }

    /**
     * Menghapus anggota berdasarkan ID anggota.
     * @param memberId ID anggota yang akan dihapus.
     */
    public void deleteMember(String memberId) {
        List<Member> members = getAllMembers();
        members.removeIf(member -> member.getMemberId().equals(memberId));
        writeAllMembers(members);
    }

    /**
     * Menulis semua anggota ke dalam file CSV.
     * @param members List berisi semua anggota yang akan ditulis.
     */
    private void writeAllMembers(List<Member> members) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_PATH))) {
            // Header baru dengan 8 kolom
            String[] header = {"memberId", "pin", "fullName", "major", "email", "gender", "addres", "phoneNumber"};
            writer.writeNext(header);
            for (Member member : members) {
                // Menulis 8 kolom
                String[] record = {
                    member.getMemberId(), member.getPin(), member.getFullName(),
                    member.getMajor(), member.getEmail(), member.getGender(),
                    member.getAddres(), member.getPhoneNumber()
                };
                writer.writeNext(record);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}